package me.noud02.akatsuki.bot

import kotlinx.coroutines.experimental.async
import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.bot.schema.Guilds
import me.noud02.akatsuki.bot.schema.Users
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.requests.SessionReconnectQueue
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Akatsuki(token: String, db_name: String, db_user: String, db_password: String) : ListenerAdapter() {
    private val eventHandler = EventHandler(this)
    private val loggr = LoggerFactory.getLogger(this::class.java)

    private val pool: ExecutorService by lazy {
        Executors.newCachedThreadPool {
            Thread(it, "Akatsuki-Pool-Thread").apply {
                isDaemon = true
            }
        }
    }
    val coroutineDispatcher by lazy {
        me.noud02.akatsuki.bot.entities.CoroutineDispatcher(pool)
    }
    private val builder: JDABuilder = JDABuilder(AccountType.BOT)
            .setToken(token)
            .addEventListener(this)
            .setReconnectQueue(SessionReconnectQueue())

    val cmdHandler = CommandHandler(this)
    val db = Database.connect("jdbc:postgresql:$db_name", "org.postgresql.Driver", db_user, db_password)

    var owners = mutableListOf<String>()
    var prefixes = mutableListOf<String>()
    var jda: JDA? = null

    init {
        async(coroutineDispatcher) {
            init()
        }
    }

    suspend fun init() {
        asyncTransaction(pool) {
            SchemaUtils.create(Guilds, Users)
        }.await()
    }

    fun build() {
        jda = builder.buildBlocking()
    }

    fun buildSharded(shards: Int, shard: Int? = null) {
        if (shard != null)
            jda = builder
                    .useSharding(shard, shards)
                    .buildAsync()
        else
            for (i in 0 until shards) {
                jda = builder
                        .useSharding(i, shards)
                        .buildAsync()
                Thread.sleep(5000)
            }
    }

    fun setGame(text: String, idle: Boolean = false) = jda!!.presence.setPresence(Game.of(text), idle)

    fun addPrefix(prefix: String) = prefixes.add(prefix)

    fun addOwner(id: String) = owners.add(id)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (prefixes.size == 0)
            prefixes = arrayListOf("!")

        if (event.guild != null)
            async(coroutineDispatcher) {
                handleGuildMessage(event)
            }
        else
            cmdHandler.handle(event)
    }

    private suspend fun handleGuildMessage(event: MessageReceivedEvent) {
        asyncTransaction(pool) {
            var res = Guilds.select {
                Guilds.id.eq(event.guild.id)
            }

            if (res.count() == 0)
                try {
                    Guilds.insert {
                        it[id] = event.guild.id
                        it[name] = event.guild.name
                        it[lang] = "en_US"
                        it[prefixes] = arrayOf("awoo!")
                    }

                    res = Guilds.select {
                        Guilds.id.eq(event.guild.id)
                    }

                    loggr.info("Added guild ${event.guild.name} to the database!")
                } catch (e: Throwable) {
                    loggr.error("Error while trying to insert guild ${event.guild.name} in DB", e)
                }

            cmdHandler.handle(event, res.first()[Guilds.prefixes])
        }.await()
    }

    override fun onReady(event: ReadyEvent) {
        loggr?.info("Ready!")
    }

    override fun onGuildJoin(event: GuildJoinEvent) = eventHandler.guildJoin(event)

    override fun onGuildLeave(event: GuildLeaveEvent) = eventHandler.guildLeave(event)
}