package akatsuki

import akatsuki.commands.Ping
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class CommandHandler constructor(private val client: Akatsuki) {
    private val commands: MutableMap<String, Command> = mutableMapOf()

    init {
        this.commands["ping"] = Ping()
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

            if (args.isNotEmpty() && this.commands[cmd]?.subcommands?.get(args[0]) is Command) {
                val subcmd = args[0]
                args = args.drop(1)
                ctx = Context(event, args)
                this.commands[cmd]?.subcommands?.get(subcmd)?.run(ctx)
            } else
                this.commands[cmd]?.run(ctx)
        }
    }
}