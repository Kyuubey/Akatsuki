/*
 *  Copyright (c) 2017 Noud Kerver
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki

import kotlinx.coroutines.experimental.async
import me.aurieh.ares.core.entities.EventWaiter
import me.noud02.akatsuki.db.DatabaseWrapper
import me.noud02.akatsuki.entities.Config
import me.noud02.akatsuki.entities.CoroutineDispatcher
import me.noud02.akatsuki.schema.Guilds
import me.noud02.akatsuki.schema.Users
import me.noud02.akatsuki.utils.Wolk
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.requests.SessionReconnectQueue
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Akatsuki(val config: Config) : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val builder = JDABuilder(AccountType.BOT)
            .setToken(config.token)
            .addEventListener(this)
            .setReconnectQueue(SessionReconnectQueue())

    lateinit var jda: JDA
    val pool: ExecutorService by lazy {
        Executors.newCachedThreadPool {
            Thread(it, "Akatsuki-Pool-Thread").apply {
                isDaemon = true
            }
        }
    }
    val coroutineDispatcher by lazy {
        CoroutineDispatcher(pool)
    }
    val waiter = EventWaiter()
    val cmdHandler = CommandHandler(this)
    val db = Database.connect(
            "jdbc:postgresql:${config.database.name}",
            "org.postgresql.Driver",
            config.database.user,
            config.database.pass
    )

    var owners = config.owners
    var prefixes = config.prefixes

    init {
        Wolk.setToken(config.api.weebsh)
        transaction {
            SchemaUtils.create(Guilds, Users)
        }
    }

    fun build() {
        jda = builder.buildAsync()
    }

    fun buildSharded(shards: Int, shard: Int? = null) {
        if (shard != null) {
            jda = builder
                    .useSharding(shard, shards)
                    .buildAsync()
        } else
            for (i in 0 until shards) {
                jda = builder
                        .useSharding(i, shards)
                        .buildAsync()
                
                Thread.sleep(5000)
            }
    }

    fun setGame(text: String, idle: Boolean = false) = jda.presence.setPresence(Game.of(text), idle)

    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (prefixes.isEmpty())
            prefixes = listOf("akatsuki ")

        if (event.author.isBot)
            return

        async(coroutineDispatcher) {
            try {
                cmdHandler.handleMessage(event)
            } catch (e: Exception) {
                logger.error("Error while trying to handle message", e)
            }
        }
    }

    override fun onReady(event: ReadyEvent) = logger.info("Ready!")

    override fun onGuildJoin(event: GuildJoinEvent) {
        logger.info("New guild: ${event.guild.name} (${event.guild.id})")

        async(coroutineDispatcher) {
            DatabaseWrapper.newGuild(event.guild)
        }
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        logger.info("Left guild: ${event.guild.name} (${event.guild.id}")

        /* TODO add remGuild function
        async(coroutineDispatcher) {
            DatabaseWrapper.remGuild(event.guild)
        }*/
    }
}
