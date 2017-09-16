package me.noud02.akatsuki.bot.entities

import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context(private val event: MessageReceivedEvent, val args: MutableMap<String, Any>/*, val flags: Array<Flag>*/) {
    val guild: Guild = this.event.guild
    val author: User = this.event.author
    val channel: MessageChannel = this.event.channel
    val msg: Message = this.event.message
    val member: Member = this.event.member

    fun send(arg: String) {
        return this.event.channel.sendMessage(arg).queue()
    }
}