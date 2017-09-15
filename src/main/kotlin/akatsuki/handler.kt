package akatsuki

import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class CommandHandler constructor(private val client: Akatsuki) {
    val commands: MutableMap<String, Command> = mutableMapOf()

    fun handle(event: MessageReceivedEvent) {
        if (event.author.isBot)
            return
        if (event.message.content.startsWith(client.botPrefix)) {
            // TODO: Actually run the command here

            val cmd: String = event.message.content.substring(client.botPrefix.length).split(" ")[0]
            println(cmd)
        }
    }
}