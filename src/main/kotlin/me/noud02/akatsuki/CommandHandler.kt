/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki

import io.sentry.Sentry
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.UserBuilder
import me.aurieh.ares.utils.ArgParser
import me.noud02.akatsuki.annotations.*
import me.noud02.akatsuki.db.DatabaseWrapper
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.extensions.UTF8Control
import me.noud02.akatsuki.extensions.searchMembers
import me.noud02.akatsuki.extensions.searchTextChannels
import me.noud02.akatsuki.utils.UserPicker
import me.noud02.akatsuki.utils.I18n
import me.noud02.akatsuki.utils.Logger
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
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.jvmName

class CommandHandler {
    private val logger = Logger(this::class.jvmName)
    private val aliases = mutableMapOf<String, String>()

    val commands = mutableMapOf<String, Command>()

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
        val guild = if (event.guild != null) DatabaseWrapper.getGuildSafe(event.guild) else null
        val user = DatabaseWrapper.getUserSafe(event.author)

        val locale = if (guild != null && guild.forceLang)
            Locale(guild.lang.split("_")[0], guild.lang.split("_")[1])
        else
            Locale(user.lang.split("_")[0], user.lang.split("_")[1])

        val guildPrefixes = guild?.prefixes ?: listOf()

        val lang = ResourceBundle.getBundle("i18n.Kyubey", locale, UTF8Control())

        val usedPrefix = Akatsuki.instance.config.prefixes.lastOrNull {
            event.message.contentRaw.startsWith(it)
        } ?: guildPrefixes.lastOrNull {
            event.message.contentRaw.startsWith(it)
        } ?: return

        var cmd = event.message.contentRaw.substring(usedPrefix.length).split(" ")[0]
        var args = event.message.contentRaw.substring(usedPrefix.length).split(" ")

        if (args.isNotEmpty())
            args = args.drop(1)

        val newPerms: MutableMap<String, Boolean>

        if (!commands.contains(cmd))
            if (aliases.contains(cmd))
                cmd = aliases[cmd] as String
            else
                return

        if (event.guild != null
                && DatabaseWrapper.getGuildSafe(event.guild).ignoredChannels.contains(event.channel.idLong)
                && cmd != "unignore")
            return

        logger.command(event)

        Sentry.getContext().apply {
            recordBreadcrumb(
                    BreadcrumbBuilder().apply {
                        setMessage("Command executed")
                        setData(mapOf(
                                "command" to cmd
                        ))
                    }.build()
            )
            setUser(
                    UserBuilder().apply {
                        setUsername(event.author.name)
                        setData(mapOf(
                                Pair("guildId", event.guild?.id ?: ""),
                                Pair("userId", event.author.id),
                                Pair("channelId", event.channel.id)
                        ))
                    }.build()
            )
        }

        var command = commands[cmd] as Command

        if (command.ownerOnly && !Akatsuki.instance.config.owners.contains(event.author.id))
            return

        if (command.guildOnly && event.guild == null)
            return event.channel.sendMessage("This command can only be used in a server!").queue() // TODO add translations for this

        if (command.nsfw && !event.textChannel.isNSFW)
            return event.channel.sendMessage("This command can only be used in a NSFW channel!").queue() // TODO add translations for this

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
                checkArguments(event, command, args, lang).thenAccept {
                    try {
                        command.run(Context(event, command, it, raw, flags, newPerms, lang, user, guild))
                    } catch (e: Exception) {
                        event.channel.sendMessage(
                                I18n.parse(
                                        lang.getString("error"),
                                        mapOf(
                                                "error" to "$e"
                                                /*"error" to "$e\n${e.stackTrace.joinToString("\n") {
                                                    "\tat ${it.className}(${it.fileName ?: "Unknown Source"})"
                                                }}"*/
                                        )
                                )
                        ).queue()

                        logger.error(
                                "Error while handling command $cmd, executed by user ${
                                event.author.name
                                }#${
                                event.author.discriminator
                                } (${
                                event.author.id
                                } in ${
                                if (event.guild != null)
                                    "guild ${event.guild.name} (${event.guild.id}) with channel ${event.channel.name} (${event.channel.id}"
                                else
                                    "DMs"
                                }",
                                e
                        )

                        Sentry.capture(e)
                    }
                }
            } catch (err: Exception) {
                return event.channel.sendMessage(err.message).queue()
            }
        } else {
            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help")))
                return event.channel.sendMessage(help(cmd)).queue()

            try {
                newPerms = checkPermissions(event, commands[cmd] as Command, lang)
                checkArguments(event, commands[cmd] as Command, args, lang).thenAccept {
                    try {
                        command.run(Context(event, command, it, raw, flags, newPerms, lang, user, guild))
                    } catch (e: Exception) {
                        event.channel.sendMessage(
                                I18n.parse(
                                        lang.getString("error"),
                                        mapOf(
                                                "error" to "$e"
                                                /*"error" to "$e\n${e.stackTrace.joinToString("\n") {
                                                    "\tat ${it.className}(${it.fileName ?: "Unknown Source"})"
                                                }}"*/
                                        )
                                )
                        ).queue()

                        logger.error(
                                "Error while handling command $cmd, executed by user ${
                                event.author.name
                                }#${
                                event.author.discriminator
                                } (${
                                event.author.id
                                } in ${
                                if (event.guild != null)
                                    "guild ${event.guild.name} (${event.guild.id}) with channel ${event.channel.name} (${event.channel.id}"
                                else
                                    "DMs"
                                }",
                                e
                        )

                        Sentry.capture(e)
                    }
                }
            } catch (err: Exception) {
                return event.channel.sendMessage(err.message).queue()
            }
        }
    }

    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command, lang: ResourceBundle): MutableMap<String, Boolean> {
        val perms: List<Perm> = cmd::class.annotations.filterIsInstance(Perm::class.java)
        val newPerms = mutableMapOf<String, Boolean>()

        for (perm in perms) {
            newPerms[perm.name.name] = event.member?.hasPermission(event.channel as Channel, perm.name)
                    ?: event.member?.hasPermission(Permission.ADMINISTRATOR) ?: false
            if (!perm.optional && !newPerms[perm.name.name]!!
                    && !event.member?.hasPermission(Permission.ADMINISTRATOR)!!
                    && !Akatsuki.instance.config.owners.contains(event.member.user.id))
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

    private fun checkArguments(
            event: MessageReceivedEvent,
            cmd: Command,
            args: List<String>,
            lang: ResourceBundle
    ): CompletableFuture<MutableMap<String, Any>> {
        val newArgs = mutableMapOf<String, Any>()
        val fut = CompletableFuture<MutableMap<String, Any>>()

        val cmdArgs = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val other = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (other.isNotEmpty())
            cmdArgs += other.first().args

        var i = 0

        fun next() {
            if (i == cmdArgs.size)  {
                fut.complete(newArgs)
                return
            }

            val arg = cmdArgs[i]
            var arg2: String?
            try {
                arg2 = args[i]
            } catch(e: Exception) {
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
                else {
                    fut.complete(newArgs)
                    return
                }
            }

            if (cmdArgs.last() == arg)
                arg2 = args.slice(cmdArgs.indexOf(arg) until args.size).joinToString(" ")

            when (arg.type) {
                "textchannel" -> {
                    if (event.guild != null)
                        when {
                            "<#\\d+>".toRegex().matches(arg2) -> try {
                                newArgs[arg.name] = event.guild.getTextChannelById("<#(\\d+)>"
                                                .toRegex()
                                                .matchEntire(arg2)?.groupValues?.get(1))
                                i++
                                next()
                            } catch (e: Throwable) {
                                throw Exception("Couldn't find that channel!")
                            }

                            event.guild.searchTextChannels(arg2).isNotEmpty() -> {
                                val channels = event.guild.searchTextChannels(arg2)

                                if (channels.size > 1) {
                                    val picker = TextChannelPicker(
                                            EventListener.instance.waiter,
                                            event.member,
                                            channels,
                                            event.guild
                                    )

                                    picker.build(event.message).thenAccept {
                                        newArgs[arg.name] = it
                                        i++
                                        next()
                                    }
                                } else {
                                    newArgs[arg.name] = channels[0]
                                    i++
                                    next()
                                }
                            }

                            else -> throw Exception(
                                    I18n.parse(
                                            lang.getString("channel_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )
                        }
                }

                "user" -> {
                    if (event.guild != null)
                        when {
                            "<@!?\\d+>".toRegex().matches(arg2) -> try {
                                newArgs[arg.name] = event.guild.getMemberById("<@!?(\\d+)>".toRegex().matchEntire(arg2)?.groupValues?.get(1))
                                i++
                                next()
                            } catch (e: Throwable) {
                                throw Exception("Couldn't find that user!")
                            }

                            event.guild.searchMembers(arg2).isNotEmpty() -> {
                                val users = event.guild.searchMembers(arg2)

                                if (users.size > 1) {
                                    val picker = UserPicker(EventListener.instance.waiter, event.member, users, event.guild)

                                    picker.build(event.message).thenAccept {
                                        newArgs[arg.name] = it
                                        i++
                                        next()
                                    }
                                } else {
                                    newArgs[arg.name] = users[0]
                                    i++
                                    next()
                                }
                            }

                            else -> throw Exception(
                                    I18n.parse(
                                            lang.getString("user_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )
                        }
                }

                "number" -> {
                    newArgs[arg.name] = arg2.toIntOrNull()
                            ?: throw Exception("Argument at pos ${i + 1} needs type 'number' but type 'string' was given")
                    i++
                    next()
                }

                else -> {
                    newArgs[arg.name] = arg2
                    i++
                    next()
                }
            }
        }

        next()

        return fut
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
            flags += otherFlags.first().flags

        val sub = cmd.subcommands.map {
            "\t${it.key}" + " ".repeat(20 - it.key.length) + "${it.value.desc.split("\n")[0]}\n"
        }
        val flag = flags.map {
            "\t-${it.abbr}, --${it.flag}${" ".repeat(20 - "-${it.abbr}, --${it.flag}".length)}${it.desc}\n"
        }
        val usage = args.map {
            if (it.optional)
                "[${it.name}: ${it.type}]"
            else
                "<${it.name}: ${it.type}>"
        }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}" else ""
        val formattedFlags = if (flag.isNotEmpty()) flag.joinToString("\n") else ""

        return "```\n" +
                "$cmdd ${usage.joinToString(" ")}\n" +
                "\n" +
                "${cmd.desc}\n" +
                "$formattedSubs\n" +
                "Flags:\n" +
                "\t-h, --help\n" +
                "$formattedFlags```"
    }

    fun help(cmd: Command): String {
        val args = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val otherArgs = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (otherArgs.isNotEmpty())
            args += otherArgs.first().args

        val flags = cmd::class.annotations.filterIsInstance(Flag::class.java).toMutableList()
        val otherFlags = cmd::class.annotations.filterIsInstance(Flags::class.java)

        if (otherFlags.isNotEmpty())
            flags += otherFlags.first().flags

        val sub = cmd.subcommands.map {
            "\t${it.key}" + " ".repeat(20 - it.key.length) + "${it.value.desc.split("\n")[0]}\n"
        }
        val flag = flags.map {
            "\t-${it.abbr}, --${it.flag}${" ".repeat(20 - "-${it.abbr}, --${it.flag}".length)}${it.desc}\n"
        }
        val usage = args.map {
            if (it.optional)
                "[${it.name}: ${it.type}]"
            else
                "<${it.name}: ${it.type}>"
        }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}" else ""
        val formattedFlags = if (flag.isNotEmpty()) flag.joinToString("\n") else ""

        val name = (if (cmd.name.isEmpty()) cmd::class.simpleName!! else cmd.name).toLowerCase()

        return "```\n" +
                "$name ${usage.joinToString(" ")}\n" +
                "\n" +
                "${cmd.desc}\n" +
                "$formattedSubs\n" +
                "Flags:\n" +
                "\t-h, --help\n" +
                "$formattedFlags```"
    }
}