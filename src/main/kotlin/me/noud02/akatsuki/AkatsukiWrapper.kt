package me.noud02.akatsuki

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class AkatsukiWrapper(token: String): ListenerAdapter() {
    val jda: JDA = JDABuilder(AccountType.BOT)
            .setToken(token)
            .addEventListener(this)
            .buildBlocking()

    override fun onReady(event: ReadyEvent) {
        println("Ready!")
    }
}