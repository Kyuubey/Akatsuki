package akatsuki

import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context constructor(private val event: MessageReceivedEvent, val args: List<String>/*, val flags: Array<Flag>*/) {
    val guild: Guild = this.event.guild
    val author: User = this.event.author
    val channel: Channel = this.event.textChannel
    val privateChannel: PrivateChannel = this.event.privateChannel
    val msg: Message = this.event.message
    val member: Member = this.event.member

    fun send(arg: String) {
        return this.event.channel.sendMessage(arg).queue()
    }
}