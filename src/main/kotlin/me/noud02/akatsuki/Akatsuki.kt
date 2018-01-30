/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki

import io.sentry.Sentry
import io.sentry.SentryClientFactory
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.db.schema.*
import me.noud02.akatsuki.entities.Config
import me.noud02.akatsuki.entities.CoroutineDispatcher
import me.noud02.akatsuki.utils.Wolk
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import okhttp3.*
import org.jetbrains.exposed.sql.*
import org.json.JSONObject
import java.util.*
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class Akatsuki(val config: Config) {
    private lateinit var presenceTimer: Timer
    lateinit var shardManager: ShardManager
    var jda: JDA? = null

    val pool: ExecutorService by lazy {
        Executors.newCachedThreadPool {
            Thread(it, "Akatsuki-Main-Pool-Thread").apply {
                isDaemon = true
            }
        }
    }
    val coroutineDispatcher by lazy {
        CoroutineDispatcher(pool)
    }
    val sentry = SentryClientFactory.sentryClient()
    val okhttp = OkHttpClient()

    init {
        Akatsuki.instance = this

        Sentry.init(config.api.sentry)
        Database.connect(
                "jdbc:postgresql://${config.database.host}/${config.database.name}",
                "org.postgresql.Driver",
                config.database.user,
                config.database.pass
        )

        Wolk.setToken(config.api.weebsh)
        asyncTransaction(pool) {
            SchemaUtils.create(Guilds, Users, Starboard, Logs, Modlogs, Contracts, Tags, Reminders)
        }.execute()
    }

    private fun startReminderChecker(checkDelay: Long = 60) {
        async(coroutineDispatcher) {
            while (isActive) {
                delay(checkDelay, TimeUnit.SECONDS)
                asyncTransaction(pool) {
                    val now = System.currentTimeMillis()
                    val results = Reminders.select { Reminders.timestamp.less(now).or(Reminders.timestamp.eq(now)) }

                    results.forEach {
                        (if (jda != null)
                            jda!!.getTextChannelById(it[Reminders.channelId])
                        else
                            shardManager.getTextChannelById(it[Reminders.channelId]))?.sendMessage(
                                "Hello <@${it[Reminders.userId]}>! You wanted me to remind you ${it[Reminders.reminder]}"
                        )?.queue()

                        Reminders.deleteWhere {
                            Reminders.userId
                                    .eq(it[Reminders.userId])
                                    .and(Reminders.reminder.eq(it[Reminders.reminder]))
                                    .and(Reminders.timestamp.eq(it[Reminders.timestamp]))
                        }
                    }
                }.await()
            }
        }
    }

    private fun startPresenceTimer() {
        presenceTimer = timer("presenceTimer", true, Date(), 60000L) {
            val presence = config.presences[Math.floor(Math.random() * config.presences.size).toInt()]
            val gameType = when(presence.type) {
                "streaming" -> Game.GameType.STREAMING

                "listening" -> Game.GameType.LISTENING

                "watching" -> Game.GameType.WATCHING

                "default" -> Game.GameType.DEFAULT
                "playing" -> Game.GameType.DEFAULT
                else -> Game.GameType.DEFAULT
            }

            if (jda != null)
                jda!!.presence.setPresence(Game.of(gameType, presence.text), false)
            else
                shardManager.setGame(Game.of(gameType, presence.text))

        }
    }

    fun updateStats() {
        if (jda != null) {
            val json = mutableMapOf(
                    "server_count" to jda!!.guilds.size
            )

            if (config.api.discordbots.isNotEmpty())
                okhttp.newCall(Request.Builder().apply {
                    post(RequestBody.create(MediaType.parse("application/json"), JSONObject(json).toString()))
                    url(HttpUrl.Builder().apply {
                        scheme("https")
                        host("bots.discord.pw")
                        addPathSegment("api")
                        addPathSegment("bots")
                        addPathSegment(jda!!.selfUser.id)
                        addPathSegment("stats")
                    }.build())

                    addHeader("Authorization", config.api.discordbots)
                }.build()).execute().close()

            if (config.api.discordbotsorg.isNotEmpty())
                okhttp.newCall(Request.Builder().apply {
                    post(RequestBody.create(MediaType.parse("application/json"), JSONObject(json).toString()))
                    url(HttpUrl.Builder().apply {
                        scheme("https")
                        host("discordbots.org")
                        addPathSegment("api")
                        addPathSegment("bots")
                        addPathSegment(jda!!.selfUser.id)
                        addPathSegment("stats")
                    }.build())

                    addHeader("Authorization", config.api.discordbotsorg)
                }.build()).execute().close()
        } else
            for (shard in shardManager.shards) {
                val json = mapOf(
                        "server_count" to shard.guilds.size,
                        "shard_id" to shard.shardInfo.shardId,
                        "shard_count" to shardManager.shardsTotal
                )

                if (config.api.discordbots.isNotEmpty())
                    okhttp.newCall(Request.Builder().apply {
                        post(RequestBody.create(MediaType.parse("application/json"), JSONObject(json).toString()))
                        url(HttpUrl.Builder().apply {
                            scheme("https")
                            host("bots.discord.pw")
                            addPathSegment("api")
                            addPathSegment("bots")
                            addPathSegment(shard.selfUser.id)
                            addPathSegment("stats")
                        }.build())

                        addHeader("Authorization", config.api.discordbots)
                    }.build()).execute().close()

                if (config.api.discordbotsorg.isNotEmpty())
                    okhttp.newCall(Request.Builder().apply {
                        post(RequestBody.create(MediaType.parse("application/json"), JSONObject(json).toString()))
                        url(HttpUrl.Builder().apply {
                            scheme("https")
                            host("discordbots.org")
                            addPathSegment("api")
                            addPathSegment("bots")
                            addPathSegment(shard.selfUser.id)
                            addPathSegment("stats")
                        }.build())

                        addHeader("Authorization", config.api.discordbotsorg)
                    }.build()).execute().close()
            }
    }

    fun build() {
        jda = JDABuilder(AccountType.BOT).apply {
            setToken(config.token)
            addEventListener(EventListener())
        }.buildAsync()

        startPresenceTimer()
        startReminderChecker()
    }

    fun build(firstShard: Int, lastShard: Int, total: Int) {
        shardManager = DefaultShardManagerBuilder().apply {
            setToken(config.token)
            addEventListeners(EventListener())
            setAutoReconnect(true)
            setShardsTotal(total)
            setShards(firstShard, lastShard)
        }.build()

        startPresenceTimer()
        startReminderChecker()
    }

    companion object {
        @JvmStatic
        lateinit var instance: Akatsuki
    }
}