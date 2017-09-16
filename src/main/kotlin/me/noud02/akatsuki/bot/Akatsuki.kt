package me.noud02.akatsuki.bot

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.Database

class Akatsuki(token: String, db_name: String, db_user: String, db_password: String) : ListenerAdapter() {

    val jda: JDA = JDABuilder(AccountType.BOT)
            .setToken(token)
            .addEventListener(this)
            .buildBlocking()

    private val cmdHandler: CommandHandler = CommandHandler(this)
    private val eventHandler: EventHandler = EventHandler(this)

    var botPrefix: String = "!"
    var owners: MutableList<String> = mutableListOf()
    val db = Database.connect(db_name, driver = "org.postgresql.Driver", password = db_password, user = db_user)

    fun setPrefix(prefix: String) {
        botPrefix = prefix
    }

    fun setGame(text: String, idle: Boolean = false) {
        jda.presence.setPresence(Game.of(text), idle)
    }

    fun addOwner(id: String) {
        owners.add(id)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        cmdHandler.handle(event)
    }

    override fun onReady(event: ReadyEvent) {
        println("Ready!")
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        eventHandler.guildJoin(event)
    }
}