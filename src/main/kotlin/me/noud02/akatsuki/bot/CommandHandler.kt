package me.noud02.akatsuki.bot

import me.aurieh.ares.exposed.async.asyncTransaction
import me.aurieh.ares.utils.ArgParser
import me.noud02.akatsuki.bot.entities.*
import me.noud02.akatsuki.bot.extensions.UTF8Control
import me.noud02.akatsuki.bot.schema.Guilds
import me.noud02.akatsuki.bot.schema.Users
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.validator.routines.UrlValidator
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.util.*

class CommandHandler(private val client: Akatsuki) {
    val commands = mutableMapOf<String, Command>()
    private val aliases = mutableMapOf<String, String>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        loadAll()
    }

    private fun addCommand(cmd: Command) {
        commands[cmd.name] = cmd
    }

    private fun loadAll() {
        Reflections(ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("me.noud02.akatsuki.commands")))
                .getSubTypesOf(Command::class.java)
                .forEach {
                    if (!it.isInterface) {
                        val ann = it.annotations.filterIsInstance<Load>()
                        val aliases = it.annotations.filterIsInstance<Alias>()
                        if (ann.isNotEmpty() && ann.first().bool) {
                            val cmd = it.newInstance() as Command
                            addCommand(cmd)
                            if (aliases.isNotEmpty())
                                for (alias in aliases.first().aliases) {
                                    this.aliases[alias] = cmd.name
                                }
                        }
                    }
                }
    }

    suspend fun handleMessage(event: MessageReceivedEvent) {
        asyncTransaction(client.pool) {
            var guilds: Query? = null
            var users = Users.select {
                Users.id.eq(event.author.id)
            }

            if (users.empty())
                try {
                    Users.insert {
                        it[id] = event.author.id
                        it[username] = event.author.name
                        it[discriminator] = event.author.discriminator
                        it[lang] = "en_US"
                    }

                    users = Users.select {
                        Users.id.eq(event.author.id)
                    }

                    this@CommandHandler.logger.info("Added user ${event.author.name}#${event.author.discriminator} to the database!")
                } catch (e: Throwable) {
                    this@CommandHandler.logger.error("Error while trying to insert user in DB", e)
                }

            if (event.guild != null) {
                guilds = Guilds.select {
                    Guilds.id.eq(event.guild.id)
                }

                if (guilds.empty())
                    try {
                        Guilds.insert {
                            it[id] = event.guild.id
                            it[name] = event.guild.name
                            it[prefixes] = arrayOf(client.prefixes[0])
                            it[lang] = "en_US"
                            it[forceLang] = false
                        }

                        guilds = Guilds.select {
                            Guilds.id.eq(event.guild.id)
                        }
                        this@CommandHandler.logger.info("Added guild ${event.guild.name} to the database!")
                    } catch (e: Throwable) {
                        this@CommandHandler.logger.error("Error while trying to insert guild ${event.guild.name} in DB", e)
                    }
            }

            val guild = if (guilds != null && !guilds.empty()) guilds.first() else null
            val user = users.first()

            val locale = if (guild != null && guild[Guilds.forceLang])
                Locale(guild[Guilds.lang].split("_")[0], guild[Guilds.lang].split("_")[1])
            else
                Locale(user[Users.lang].split("_")[0], user[Users.lang].split("_")[1])

            val guildPrefixes = if (guild != null) guild[Guilds.prefixes] else arrayOf()

            val lang = ResourceBundle.getBundle("i18n.Kyubey", locale, UTF8Control())

            val usedPrefix: String? = client.prefixes.lastOrNull { event.message.rawContent.startsWith(it) } ?: guildPrefixes.lastOrNull { event.message.rawContent.startsWith(it) }

            if (usedPrefix != null) {
                var cmd: String = event.message.rawContent.substring(usedPrefix.length).split(" ")[0]
                var args: List<String> = event.message.rawContent.substring(usedPrefix.length).split(" ")

                if (args.isNotEmpty())
                    args = args.drop(1)

                val ctx: Context
                val newArgs: MutableMap<String, Any>
                val newPerms: MutableMap<String, Boolean>

                if (!commands.contains(cmd))
                    if (aliases.contains(cmd))
                        cmd = aliases[cmd] as String
                    else
                        return@asyncTransaction

                if (event.guild != null)
                    this@CommandHandler.logger.info("[Command] (Guild ${event.guild.name} (${event.guild.id})) - ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.content}")
                else
                    this@CommandHandler.logger.info("[Command] (DM) - ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.content}")

                if ((commands[cmd] as Command).ownerOnly && !client.owners.contains(event.author.id))
                    return@asyncTransaction

                if ((commands[cmd] as Command).guildOnly && event.guild == null)
                    return@asyncTransaction event.channel.sendMessage("This command can only be used in a server!").queue() // TODO add translations for this

                var command = commands[cmd] as Command

                if (args.isNotEmpty() && commands[cmd]?.subcommands?.get(args[0]) is Command) {
                    val subcmd = args[0]
                    command = commands[cmd]?.subcommands?.get(subcmd) as Command
                    args = args.drop(1)

                    val raw = args

                    val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

                    args = flags.unmatched

                    if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                        return@asyncTransaction event.channel.sendMessage(help(command)).queue()

                    try {
                        newPerms = checkPermissions(event, command, lang)
                        newArgs = checkArguments(event, command, args, lang)
                    } catch (err: Exception) {
                        return@asyncTransaction event.channel.sendMessage(err.message).queue()
                    }

                    ctx = Context(event, client, command, newArgs, raw, flags, newPerms, lang)

                    if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                        return@asyncTransaction ctx.send(ctx.help())


                    command.run(ctx)
                } else {
                    val raw = args

                    val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

                    args = flags.unmatched

                    if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                        return@asyncTransaction event.channel.sendMessage(help(command)).queue()

                    try {
                        newPerms = checkPermissions(event, commands[cmd] as Command, lang)
                        newArgs = checkArguments(event, commands[cmd] as Command, args, lang)
                    } catch (err: Exception) {
                        return@asyncTransaction event.channel.sendMessage(err.message).queue()
                    }

                    ctx = Context(event, client, commands[cmd] as Command, newArgs, raw, flags, newPerms, lang)

                    command.run(ctx)
                }
            }
        }.await()
    }

    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command, lang: ResourceBundle): MutableMap<String, Boolean> {
        val perms: List<Perm> = cmd::class.annotations.filterIsInstance(Perm::class.java)
        val newPerms = mutableMapOf<String, Boolean>()

        for (perm in perms) {
            newPerms[perm.name.name] = event.member?.hasPermission(event.channel as Channel, perm.name) ?: event.member?.hasPermission(Permission.ADMINISTRATOR) ?: false
            if (!perm.optional && !newPerms[perm.name.name]!! && !event.member?.hasPermission(Permission.ADMINISTRATOR)!!)
                throw Exception(i18n.parse(lang.getString("user_lack_perms"), mapOf("username" to event.author.name, "permission" to i18n.permission(lang, perm.name.name))))
        }

        return newPerms
    }

    private fun checkArguments(event: MessageReceivedEvent, cmd: Command, args: List<String>, lang: ResourceBundle): MutableMap<String, Any> {
        val newArgs = mutableMapOf<String, Any>()

        val cmdArgs = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val other = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (other.isNotEmpty())
            cmdArgs += other.first().args

        for (arg in cmdArgs) {
            val i = cmdArgs.indexOf(arg)
            val arg2: String?
            try {
                arg2 = args[i]
            } catch (e: IndexOutOfBoundsException) {
                if (!arg.optional)
                    throw Exception(i18n.parse(lang.getString("argument_not_specified"), mapOf("argument" to arg.name, "username" to event.author.name)))
                else
                    return newArgs
            }
            when (arg.type) {
                // TODO do these later
                // "guild" ->
                // "channel" ->
                // "role" ->
                // "voicechannel" ->
                "url" -> {
                    if (!UrlValidator().isValid(arg2))
                        throw Exception("Argument is not a valid URL!")
                    else
                        newArgs[arg.name] = arg2
                }
                "user" -> {
                    if (event.guild != null) {
                        val user: Member = when {
                            "<@!?\\d+>".toRegex().matches(arg2) -> try { event.guild.getMemberById("<@!?(\\d+)>".toRegex().matchEntire(arg2)?.groupValues?.get(1)) } catch (e: Throwable) { throw Exception("Couldn't find that user!") }
                            event.guild.getMembersByName(arg2, true).isNotEmpty() -> event.guild.getMembersByName(arg2, true)[0]
                            event.guild.getMembersByEffectiveName(arg2, true).isNotEmpty() -> event.guild.getMembersByEffectiveName(arg2, true)[0]
                            event.guild.getMemberById(arg2) != null -> event.guild.getMemberById(arg2)
                            else -> throw Exception(i18n.parse(lang.getString("user_not_found"), mapOf("username" to event.author.name)))
                        }

                        newArgs[arg.name] = user
                    }
                }
                "number" -> newArgs[arg.name] = arg2.toIntOrNull() ?: throw Exception("Argument at pos ${i + 1} needs type 'number' but type 'string' was given") // TODO translate this
                "string" -> newArgs[arg.name] = arg2
                else -> newArgs[arg.name] = arg2
            }
        }

        return newArgs
    }

    fun help(cmdd: String): String {
        if (!commands.contains(cmdd))
            throw Exception("No such command: $cmdd")

        val cmd = commands[cmdd] as Command
        val args = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val otherArgs = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (otherArgs.isNotEmpty())
            args += otherArgs.first().args

        val flags = cmd::class.annotations.filterIsInstance(Flag::class.java).toMutableList()
        val otherFlags = cmd::class.annotations.filterIsInstance(Flags::class.java)

        if (otherFlags.isNotEmpty())
            args += otherArgs.first().args

        val sub = cmd.subcommands.map { entry: Map.Entry<String, Command> -> "\t${entry.value.name}" + " ".repeat(20 - entry.value.name.length) + "${entry.value.desc}\n" }
        val flag = flags.map { flag: Flag -> "\t${flag.abbr}, ${flag.flag}" + " ".repeat(20 - "${flag.abbr}, ${flag.flag}".length) + flag.desc + "\n"}
        val usage = args.map { argument: Argument -> if (argument.optional) "[${argument.name}: ${argument.type}]" else "<${argument.name}: ${argument.type}>" }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}" else ""
        val formattedFlags = if (flag.isNotEmpty()) "\nFlags:\n${flag.joinToString("\n")}" else ""

        return "```\n${cmd.name} ${usage.joinToString(" ")}\n\n${cmd.desc}\n$formattedSubs$formattedFlags```"
    }

    fun help(cmd: Command): String {
        val args = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val otherArgs = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (otherArgs.isNotEmpty())
            args += otherArgs.first().args

        val flags = cmd::class.annotations.filterIsInstance(Flag::class.java).toMutableList()
        val otherFlags = cmd::class.annotations.filterIsInstance(Flags::class.java)

        if (otherFlags.isNotEmpty())
            args += otherArgs.first().args

        val sub = cmd.subcommands.map { entry: Map.Entry<String, Command> -> "\t${entry.value.name}" + " ".repeat(20 - entry.value.name.length) + "${entry.value.desc}\n" }
        val flag = flags.map { flag: Flag -> "\t-${flag.abbr}, --${flag.flag}" + " ".repeat(20 - "-${flag.abbr}, --${flag.flag}".length) + flag.desc + "\n"}
        val usage = args.map { argument: Argument -> if (argument.optional) "[${argument.name}: ${argument.type}]" else "<${argument.name}: ${argument.type}>" }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}" else ""
        val formattedFlags = if (flag.isNotEmpty()) "\nFlags:\n${flag.joinToString("\n")}" else ""

        return "```\n${cmd.name} ${usage.joinToString(" ")}\n\n${cmd.desc}\n$formattedSubs$formattedFlags```"
    }
}