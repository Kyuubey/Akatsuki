package me.noud02.akatsuki

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class CommandHandler constructor(private val client: Akatsuki) {
    val commands: MutableMap<String, Command> = mutableMapOf()

    fun init() {
        return
    }

    fun handle(event: MessageReceivedEvent) {
        if (event.author.isBot)
            return
        if (event.message.content.startsWith(client.botPrefix)) {
            // TODO: add message handler
        }
    }
}