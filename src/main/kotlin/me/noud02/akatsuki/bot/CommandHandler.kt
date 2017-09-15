package me.noud02.akatsuki.bot

import me.noud02.akatsuki.commands.Ping
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class CommandHandler(private val client: Akatsuki) {
    private val commands: MutableMap<String, Command> = mutableMapOf()

    init {
        commands["ping"] = Ping()
    }

    fun handle(event: MessageReceivedEvent) {
        if (event.author.isBot)
            return
        if (event.message.content.startsWith(client.botPrefix)) {
            val cmd: String = event.message.content.substring(client.botPrefix.length).split(" ")[0]
            var args: List<String> = event.message.content.substring(client.botPrefix.length).split(" ")

            if (args.isNotEmpty())
                args = args.drop(1)

            var ctx = Context(event, args)

            // TODO: Finish checkArguments

            if (args.isNotEmpty() && commands[cmd]?.subcommands?.get(args[0]) is Command) {
                val subcmd = args[0]
                args = args.drop(1)
                // checkArguments(commands[cmd]?.subcommands?.get(subcmd), args)
                ctx = Context(event, args)
                commands[cmd]?.subcommands?.get(subcmd)?.run(ctx)
            } else {
                // checkArguments(args)
                commands[cmd]?.run(ctx)
            }
        }
    }

    private fun checkArguments(cmd: Command?, args: List<String>) {
        if (cmd === null)
            return

        for (arg in cmd.args) {
            val i = cmd.args.indexOf(arg)
            val arg2: String? = args[i]

            if (arg2 === null && !arg.optional)
                throw Error("Argument at pos $i is required, but was not specified.")
        }
    }
}