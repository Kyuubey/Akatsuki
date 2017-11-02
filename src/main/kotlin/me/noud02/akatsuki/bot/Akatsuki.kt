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

package me.noud02.akatsuki.bot

import kotlinx.coroutines.experimental.async
import lavalink.client.io.Lavalink
import me.aurieh.ares.core.entities.EventWaiter
import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.bot.schema.Guilds
import me.noud02.akatsuki.bot.schema.Users
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
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Akatsuki(token: String, db_name: String, db_user: String, db_password: String) : ListenerAdapter() {
    private val eventHandler = EventHandler(this)
    private val logger = LoggerFactory.getLogger(this::class.java)

    val pool: ExecutorService by lazy {
        Executors.newCachedThreadPool {
            Thread(it, "Akatsuki-Pool-Thread").apply {
                isDaemon = true
            }
        }
    }
    val coroutineDispatcher by lazy {
        me.noud02.akatsuki.bot.entities.CoroutineDispatcher(pool)
    }

    val waiter = EventWaiter()
    private val builder: JDABuilder = JDABuilder(AccountType.BOT)
            .setToken(token)
            .addEventListener(this)
            .setReconnectQueue(SessionReconnectQueue())

    val cmdHandler = CommandHandler(this)
    val db = Database.connect("jdbc:postgresql:$db_name", "org.postgresql.Driver", db_user, db_password)

    var owners = mutableListOf<String>()
    var prefixes = mutableListOf<String>()
    var jda: JDA? = null
    var lavalink: Lavalink? = null

    init {
        async(coroutineDispatcher) {
            asyncTransaction(pool) {
                SchemaUtils.create(Guilds, Users)
            }.await()
        }
    }

    fun build() {
        jda = builder.buildBlocking()
        lavalink = Lavalink(jda!!.selfUser.id, jda!!.shardInfo.shardTotal, { jda as JDA })
    }

    fun buildSharded(shards: Int, shard: Int? = null) {
        if (shard != null) {
            jda = builder
                    .useSharding(shard, shards)
                    .buildAsync()
            lavalink = Lavalink(jda!!.selfUser.id, jda!!.shardInfo.shardTotal, { jda as JDA })
        } else {
            for (i in 0 until shards) {
                jda = builder
                        .useSharding(i, shards)
                        .buildAsync()
                Thread.sleep(5000)
                lavalink = Lavalink(jda!!.selfUser.id, jda!!.shardInfo.shardTotal, { jda as JDA })
            }
        }
    }

    fun setGame(text: String, idle: Boolean = false) = jda!!.presence.setPresence(Game.of(text), idle)

    fun addPrefix(prefix: String) = prefixes.add(prefix)

    fun addOwner(id: String) = owners.add(id)

    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (prefixes.size == 0)
            prefixes = arrayListOf("!")

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

    override fun onGuildJoin(event: GuildJoinEvent) = eventHandler.guildJoin(event)

    override fun onGuildLeave(event: GuildLeaveEvent) = eventHandler.guildLeave(event)
}