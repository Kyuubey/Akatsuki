package me.noud02.akatsuki.bot.entities

import me.aurieh.ares.utils.ArgParser
import me.noud02.akatsuki.bot.Akatsuki
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context(private val event: MessageReceivedEvent, private val client: Akatsuki, private val cmd: Command, val args: MutableMap<String, Any>, val rawArgs: List<String>, val flags: ArgParser.ParsedResult) {
    val guild: Guild? = this.event.guild
    val author: User = this.event.author
    val channel: MessageChannel = this.event.channel
    val msg: Message = this.event.message
    val member: Member? = this.event.member

    fun send(arg: Any) = this.event.channel.sendMessage(arg.toString()).queue()

    fun sendCode(lang: String, arg: Any) = this.event.channel.sendMessage("```$lang\n$arg```").queue()

    fun help() = client.cmdHandler.help(cmd)

    fun help(cmdd: String): String {
        return try {
            client.cmdHandler.help(cmdd)
        } catch (e: Exception) {
            e.message.toString()
        }
    }
}