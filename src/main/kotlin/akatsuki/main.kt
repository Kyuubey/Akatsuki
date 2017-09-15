package akatsuki

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener

fun main (args: Array<String>) {
    if (args.isEmpty())
        return println("Please provide a (valid) token!")
    val bot = Akatsuki(args[0])

    bot.setPrefix("awoo!")
}

class Akatsuki constructor(token: String) : EventListener {

    private val jda: JDA = JDABuilder(AccountType.BOT)
            .setToken(token)
            .addEventListener(this)
            .buildBlocking()
    private val handler: CommandHandler = CommandHandler(this)

    var botPrefix: String = ""

    fun setPrefix(prefix: String) {
        this.botPrefix = prefix;
    }

    override fun onEvent(event: Event) {
        when (event) {
            is ReadyEvent -> println("Ready!")
            is MessageReceivedEvent -> this.handler.handle(event)
        }
    }


}