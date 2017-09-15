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
            // TODO: Actually run the command here

            val cmd: String = event.message.content.substring(client.botPrefix.length).split(" ")[0]
            val args: List<String> = event.message.content.substring(client.botPrefix.length).split("").drop(0)
            val ctx = Context(event, args)

            this.commands[cmd]?.run(ctx)
        }
    }
}