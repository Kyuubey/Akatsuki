import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import java.util.*

class Akatsuki constructor(token: String) : EventListener {

    val jda: JDA = JDABuilder(AccountType.BOT)
            .setToken(token)
            .addEventListener(this)
            .buildBlocking()

    @Override
    fun onEvent() {
        println("Ready!")
    }
}