/*
 *  Copyright (c) 2017 Noud Kerver
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki

import kotlinx.coroutines.experimental.future.await
import me.aurieh.ares.utils.ArgParser
import me.noud02.akatsuki.annotations.*
import me.noud02.akatsuki.db.DBGuild
import me.noud02.akatsuki.db.DatabaseWrapper
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.extensions.UTF8Control
import me.noud02.akatsuki.utils.UserPicker
import me.noud02.akatsuki.utils.I18n
import me.noud02.akatsuki.utils.TextChannelPicker
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.validator.routines.UrlValidator
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.util.*

class CommandHandler {
    val commands = mutableMapOf<String, Command>()
    private val aliases = mutableMapOf<String, String>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        loadAll()
    }

    private fun addCommand(cmd: Command, name: String) {
        commands[name] = cmd
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
                            val name = (if (cmd.name.isEmpty()) cmd::class.simpleName ?: return else cmd.name).toLowerCase()
                            addCommand(cmd, name)

                            logger.info("Loaded command $name")
                            if (aliases.isNotEmpty())
                                for (alias in aliases.first().aliases) {
                                    logger.info("Added alias for command $name: $alias")
                                    this.aliases[alias] = name
                                }
                        }
                    }
                }
    }

    fun handleMessage(event: MessageReceivedEvent) {
        val guild: DBGuild? = if (event.guild != null) DatabaseWrapper.getGuildSafe(event.guild) else null
        val user = DatabaseWrapper.getUserSafe(event.author)

        val locale = if (guild != null && guild.forceLang)
            Locale(guild.lang.split("_")[0], guild.lang.split("_")[1])
        else
            Locale(user.lang.split("_")[0], user.lang.split("_")[1])

        val guildPrefixes = guild?.prefixes ?: listOf()

        val lang = ResourceBundle.getBundle("i18n.Kyubey", locale, UTF8Control())

        val usedPrefix: String = Akatsuki.client.prefixes.lastOrNull {
            event.message.rawContent.startsWith(it)
        } ?: guildPrefixes.lastOrNull {
            event.message.rawContent.startsWith(it)
        } ?: return

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
                return

        if (event.guild != null)
            logger.info("[Command] (Guild ${event.guild.name} (${event.guild.id})) - " +
                    "${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.content}")
        else
            logger.info("[Command] (DM) - ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.content}")

        if ((commands[cmd] as Command).ownerOnly && !Akatsuki.client.owners.contains(event.author.id))
            return

        if ((commands[cmd] as Command).guildOnly && event.guild == null)
            return event.channel.sendMessage("This command can only be used in a server!").queue() // TODO add translations for this

        var command = commands[cmd] as Command

        if (args.isNotEmpty() && commands[cmd]?.subcommands?.get(args[0]) is Command) {
            val subcmd = args[0]
            command = commands[cmd]?.subcommands?.get(subcmd) as Command
            args = args.drop(1)

            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                return event.channel.sendMessage(help(command)).queue()

            try {
                newPerms = checkPermissions(event, command, lang)
                newArgs = checkArguments(event, command, args, lang)
            } catch (err: Exception) {
                err.printStackTrace()
                return event.channel.sendMessage(err.message).queue()
            }

            ctx = Context(event, command, newArgs, raw, flags, newPerms, lang, user, guild)

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                return ctx.send(ctx.help())


            command.run(ctx)
        } else {
            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                return event.channel.sendMessage(help(command)).queue()

            try {
                newPerms = checkPermissions(event, commands[cmd] as Command, lang)
                newArgs = checkArguments(event, commands[cmd] as Command, args, lang)
            } catch (err: Exception) {
                err.printStackTrace()
                return event.channel.sendMessage(err.message).queue()
            }

            ctx = Context(event, commands[cmd] as Command, newArgs, raw, flags, newPerms, lang, user, guild)

            command.run(ctx)
        }
    }

    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command, lang: ResourceBundle): MutableMap<String, Boolean> {
        val perms: List<Perm> = cmd::class.annotations.filterIsInstance(Perm::class.java)
        val newPerms = mutableMapOf<String, Boolean>()

        for (perm in perms) {
            newPerms[perm.name.name] = event.member?.hasPermission(event.channel as Channel, perm.name)
                    ?: event.member?.hasPermission(Permission.ADMINISTRATOR) ?: false
            if (!perm.optional && !newPerms[perm.name.name]!! && !event.member?.hasPermission(Permission.ADMINISTRATOR)!!)
                throw Exception(
                        I18n.parse(
                                lang.getString("user_lack_perms"),
                                mapOf(
                                        "username" to event.author.name,
                                        "permission" to I18n.permission(lang, perm.name.name)
                                )
                        )
                )
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
            var arg2: String?
            try {
                arg2 = args[i]
            } catch (e: Exception) {
                if (!arg.optional)
                    throw Exception(
                            I18n.parse(
                                    lang.getString("argument_not_specified"),
                                    mapOf(
                                            "argument" to arg.name,
                                            "username" to event.author.name
                                    )
                            )
                    )
                else
                    return newArgs
            }

            if (cmdArgs.size == 1)
                arg2 = args.joinToString(" ")

            when (arg.type) {
                // TODO do these later
                // "channel" -> TODO()
                // "role" -> TODO()

                "textchannel" -> {
                    if (event.guild != null) {
                        val channel: TextChannel = when {
                            "<#\\d+>".toRegex().matches(arg2) -> try {
                                event.guild.getTextChannelById("<#(\\d+)>".toRegex().matchEntire(arg2)?.groupValues?.get(1))
                            } catch (e: Throwable) {
                                throw Exception("Couldn't find that channel!")
                            }

                            event.guild.getTextChannelsByName(arg2, true).isNotEmpty() -> {
                                val channels = event.guild.getTextChannelsByName(arg2, true)

                                if (channels.size > 1) {
                                    val picker = TextChannelPicker(Akatsuki.client.waiter, event.member, channels, event.guild)

                                    picker.build(event.message).get()
                                } else
                                    channels[0]
                            }

                            arg2.toLongOrNull() != null && event.guild.getTextChannelById(arg2) != null -> event.guild.getTextChannelById(arg2)

                            else -> throw Exception(
                                    I18n.parse(
                                            lang.getString("channel_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )
                        }

                        newArgs[arg.name] = channel
                    }
                }

                "url" -> {
                    if (!UrlValidator().isValid(arg2))
                        throw Exception("Argument is not a valid URL!")
                    else
                        newArgs[arg.name] = arg2
                }
                "user" -> {
                    if (event.guild != null) {
                        val user: Member = when {
                            "<@!?\\d+>".toRegex().matches(arg2) -> try {
                                event.guild.getMemberById("<@!?(\\d+)>".toRegex().matchEntire(arg2)?.groupValues?.get(1))
                            } catch (e: Throwable) {
                                throw Exception("Couldn't find that user!")
                            }

                            event.guild.getMembersByEffectiveName(arg2, true).isNotEmpty() -> {
                                val users = event.guild.getMembersByEffectiveName(arg2, true)
                                if (users.size > 1) {
                                    val picker = UserPicker(Akatsuki.client.waiter, event.member, users, event.guild)

                                    picker.build(event.channel).get()
                                } else
                                    users[0]
                            }

                            event.guild.getMembersByName(arg2, true).isNotEmpty() -> {
                                val users = event.guild.getMembersByName(arg2, true)
                                if (users.size > 1) {
                                    val picker = UserPicker(Akatsuki.client.waiter, event.member, users, event.guild)

                                    picker.build(event.channel).get()
                                } else
                                    users[0]
                            }

                            arg2.toLongOrNull() != null && event.guild.getMemberById(arg2) != null -> event.guild.getMemberById(arg2)

                            else -> throw Exception(
                                    I18n.parse(
                                            lang.getString("user_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )
                        }

                        newArgs[arg.name] = user
                    }
                }
                "number" -> newArgs[arg.name] = arg2.toIntOrNull()
                        ?: throw Exception("Argument at pos ${i + 1} needs type 'number' but type 'string' was given") // TODO translate this
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

        val sub = cmd.subcommands.map {
            "\t${it.key}" + " ".repeat(20 - it.key.length) + "${it.value.desc}\n"
        }
        val flag = flags.map {
            "\t${it.abbr}, ${it.flag}${" ".repeat(20 - "${it.abbr}, ${it.flag}".length)}${it.desc}\n"
        }
        val usage = args.map {
            if (it.optional)
                "[${it.name}: ${it.type}]"
            else
                "<${it.name}: ${it.type}>"
        }
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

        val sub = cmd.subcommands.map {
            "\t${it.key}" + " ".repeat(20 - it.key.length) + "${it.value.desc}\n"
        }
        val flag = flags.map {
            "\t${it.abbr}, ${it.flag}${" ".repeat(20 - "${it.abbr}, ${it.flag}".length)}${it.desc}\n"
        }
        val usage = args.map {
            if (it.optional)
                "[${it.name}: ${it.type}]"
            else
                "<${it.name}: ${it.type}>"
        }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}" else ""
        val formattedFlags = if (flag.isNotEmpty()) "\nFlags:\n${flag.joinToString("\n")}" else ""

        return "```\n${cmd.name} ${usage.joinToString(" ")}\n\n${cmd.desc}\n$formattedSubs$formattedFlags```"
    }
}