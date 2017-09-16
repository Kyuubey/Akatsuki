package me.noud02.akatsuki.bot

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Perm
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

class CommandHandler(private val client: Akatsuki) {
    private val commands = mutableMapOf<String, Command>()

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
                    if (!it.isInterface)
                        addCommand(it.newInstance() as Command)
                }
    }

    fun handle(event: MessageReceivedEvent) {
        if (event.author.isBot)
            return

        val usedPrefix: String? = client.prefixes.lastOrNull { event.message.content.startsWith(it) }

        if (usedPrefix != null) {
            val cmd: String = event.message.content.substring(usedPrefix.length).split(" ")[0]
            var args: List<String> = event.message.content.substring(usedPrefix.length).split(" ")

            if (args.isNotEmpty())
                args = args.drop(1)

            val ctx: Context
            val newArgs: MutableMap<String, Any>

            if (!commands.contains(cmd))
                return

            if ((commands[cmd] as Command).ownerOnly && !client.owners.contains(event.author.id))
                return

            if (args.isNotEmpty() && commands[cmd]?.subcommands?.get(args[0]) is Command) {
                val subcmd = args[0]
                args = args.drop(1)

                try {
                    checkPermissions(event, commands[cmd]?.subcommands?.get(subcmd))
                    newArgs = checkArguments(event, commands[cmd]?.subcommands?.get(subcmd), args)
                } catch (err: Exception) {
                    event.channel.sendMessage(err.message).complete()
                    return
                }

                ctx = Context(event, client, newArgs, args)
                commands[cmd]?.subcommands?.get(subcmd)?.run(ctx)
            } else {
                try {
                    checkPermissions(event, commands[cmd])
                    newArgs = checkArguments(event, commands[cmd], args)
                } catch (err: Exception) {
                    event.channel.sendMessage(err.message).complete()
                    return
                }

                ctx = Context(event, client, newArgs, args)

                commands[cmd]?.run(ctx)
            }
        }
    }

    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command?) {
        if (cmd == null)
            throw Exception("Command is null")

        val perms: List<Perm> = cmd::class.annotations.filterIsInstance(Perm::class.java)
        val newPerms = mutableMapOf<Permission, Boolean>()

        for (perm in perms) {
            newPerms[perm.name] = event.member?.hasPermission(event.channel as Channel, perm.name) ?: false
            if (!perm.optional && newPerms.contains(perm.name))
                throw Exception("Permission `${perm.name.name}` is required but was not found!")
        }
    }

    private fun checkArguments(event: MessageReceivedEvent, cmd: Command?, args: List<String>): MutableMap<String, Any> {
        if (cmd == null)
            throw Exception("Command is null")

        val newArgs = mutableMapOf<String, Any>()

        val cmdArgs: List<Argument> = cmd::class.annotations.filterIsInstance(Argument::class.java)

        for (arg in cmdArgs) {
            val i = cmdArgs.indexOf(arg)
            val arg2: String?
            try {
                arg2 = args[i]
            } catch (e: IndexOutOfBoundsException) {
                if (!arg.optional)
                    throw Exception("Argument at pos ${i + 1} is required, but was not specified.")
                else
                    return newArgs
            }
            when (arg.type) {
                // TODO: do these later
                // "guild" ->
                // "channel" ->
                // "role" ->
                // "voicechannel" ->
                "user" -> {
                    // TODO: Add mention using regex
                    if (event.guild != null) {
                        val user: Member = when {
                            event.guild.getMembersByName(arg2, true).isNotEmpty() -> event.guild.getMembersByName(arg2, true)[0]
                            event.guild.getMembersByEffectiveName(arg2, true).isNotEmpty() -> event.guild.getMembersByEffectiveName(arg2, true)[0]
                            event.guild.getMemberById(arg2) != null -> event.guild.getMemberById(arg2)
                            else -> throw Exception("Couldn't find that user!")
                        }

                        newArgs[arg.name] = user
                    }
                }
                "number" -> newArgs[arg.name] = arg2.toIntOrNull() ?: throw Exception("Argument at pos ${i + 1} needs type 'number' but type 'string' was given")
                "string" -> newArgs[arg.name] = arg2
                else -> newArgs[arg.name] = arg2
            }
        }

        return newArgs
    }
}