package me.noud02.akatsuki.bot

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

class CommandHandler(private val client: Akatsuki) {
    private val commands: MutableMap<String, Command> = mutableMapOf()

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

        if (event.message.content.startsWith(client.botPrefix)) {
            val cmd: String = event.message.content.substring(client.botPrefix.length).split(" ")[0]
            var args: List<String> = event.message.content.substring(client.botPrefix.length).split(" ")

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
                    newArgs = checkArguments(event, commands[cmd]?.subcommands?.get(subcmd), args)
                } catch (err: Exception) {
                    event.channel.sendMessage(err.message).complete()
                    return
                }

                ctx = Context(event, newArgs, args)
                commands[cmd]?.subcommands?.get(subcmd)?.run(ctx)
            } else {
                try {
                    newArgs = checkArguments(event, commands[cmd], args)
                } catch (err: Exception) {
                    event.channel.sendMessage(err.message).complete()
                    return
                }

                ctx = Context(event, newArgs, args)

                commands[cmd]?.run(ctx)
            }
        }
    }

    private fun checkArguments(event: MessageReceivedEvent, cmd: Command?, args: List<String>): MutableMap<String, Any> {
        if (cmd == null)
            throw Exception("Command is null")

        val newArgs: MutableMap<String, Any> = mutableMapOf()

        for (arg in cmd.args) {
            val i = cmd.args.indexOf(arg)
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