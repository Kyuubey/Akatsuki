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

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.db.schema.*
import me.noud02.akatsuki.entities.Config
import me.noud02.akatsuki.entities.CoroutineDispatcher
import me.noud02.akatsuki.utils.Wolk
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Akatsuki(val config: Config) {
    lateinit var shardManager: ShardManager

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

    init {
        Akatsuki.instance = this

        Database.connect(
                "jdbc:postgresql://${config.database.host}/${config.database.name}",
                "org.postgresql.Driver",
                config.database.user,
                config.database.pass
        )

        Wolk.setToken(config.api.weebsh)
        asyncTransaction(pool) {
            SchemaUtils.create(Guilds, Users, Starboard, Logs, Modlogs)
        }.execute()
    }

    fun build() {
        JDABuilder(AccountType.BOT).apply {
            setToken(config.token)
            addEventListener(EventListener())
        }.buildAsync()
    }

    fun build(firstShard: Int, lastShard: Int, total: Int) {
        shardManager = DefaultShardManagerBuilder().apply {
            setToken(config.token)
            addEventListeners(EventListener())
            setAutoReconnect(true)
            setShardsTotal(total)
            setShards(firstShard, lastShard)
        }.build()
    }

    companion object {
        @JvmStatic
        lateinit var instance: Akatsuki
    }
}

/*
class Akatsuki(val config: Config) : ListenerAdapter() {
    private val logger = Logger(this::class)
    private val builder = JDABuilder(AccountType.BOT)
            .setToken(config.token)
            .addEventListener(this)
            .setAutoReconnect(true)
    private lateinit var presenceTimer: Timer

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
    val cmdHandler = CommandHandler()
    val db = Database.connect(
            "jdbc:postgresql://${config.database.host}/${config.database.name}",
            "org.postgresql.Driver",
            config.database.user,
            config.database.pass
    )
    val snipes = mutableMapOf<Long, Long>()

    var owners = config.owners
    var prefixes = config.prefixes

    init {
        Wolk.setToken(config.api.weebsh)
        asyncTransaction(pool) {
            SchemaUtils.create(Guilds, Users, Starboard, Logs, Modlogs)
        }.execute()
        Akatsuki.client = this
    }

    fun build() {
        jda = builder.buildAsync()

        startPresenceTimer()
    }

    fun buildSharded(shards: Int, shard: Int? = null) {
        if (shard != null) {
            jda = builder
                    .useSharding(shard, shards)
                    .buildAsync()
        } else {
            for (i in 0 until shards) {
                jda = builder
                        .useSharding(i, shards)
                        .buildAsync()
            }
        }

        startPresenceTimer()
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

            jda.presence.setPresence(
                    Game.of(gameType, presence.text),
                    false
            )
        }
    }

    fun updateBotlist() {
        if (config.api.discordbots.isNotEmpty()) {
            val json = mutableMapOf(
                    "server_count" to jda.guilds.size
            )

            var req = khttp.post(
                    "https://bots.discord.pw/api/bots/${jda.selfUser.id}/stats",
                    json = json
            )
        }
    }

    fun setGame(text: String, idle: Boolean = false) = jda.presence.setPresence(Game.of(Game.GameType.DEFAULT, text), idle)

    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.guild != null && DatabaseWrapper.getGuildSafe(event.guild).logs)
            event.message.log()

        if (prefixes.isEmpty())
            prefixes = listOf("akatsuki ")

        if (event.author.isBot)
            return

        try {
            cmdHandler.handleMessage(event)
        } catch (e: Exception) {
            logger.error("Error while trying to handle message", e)
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.guild != null && DatabaseWrapper.getGuildSafe(event.guild).logs)
            DatabaseWrapper.logEvent(event)
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (event.guild != null && DatabaseWrapper.getGuildSafe(event.guild).logs)
            event.message.log("UPDATE")
    }

    override fun onReady(event: ReadyEvent) = logger.info("Ready!")

    override fun onGuildJoin(event: GuildJoinEvent) {
        logger.info("New guild: ${event.guild.name} (${event.guild.id})")

        DatabaseWrapper.newGuild(event.guild)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        logger.info("Left guild: ${event.guild.name} (${event.guild.id}")

        DatabaseWrapper.remGuild(event.guild)
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.guild != null && event.reaction.reactionEmote.name == "\u2b50") {
            val guild = DatabaseWrapper.getGuildSafe(event.guild)

            if (!guild.starboard)
                return

            val msg = event.channel.getMessageById(event.messageId).complete()

            event.guild.addStar(msg, event.user)
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (event.guild != null && event.reaction.reactionEmote.name == "\u2b50") {
            val guild = DatabaseWrapper.getGuildSafe(event.guild)

            if (!guild.starboard)
                return

            val msg = event.channel.getMessageById(event.messageId).complete()

            event.guild.removeStar(msg, event.user)
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        asyncTransaction(pool) {
            val guild = Guilds.select { Guilds.id.eq(event.guild.idLong) }.firstOrNull()

            if (guild == null || !guild[Guilds.modlogs] || event.guild.getTextChannelById(guild[Guilds.modlogChannel]) == null)
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel])
            val audit = event.guild.auditLogs.type(ActionType.KICK).limit(2).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction
            val case = modlogs.count() + 1

            val msg = modlogChannel.sendMessage("""
                **Kick** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

            Modlogs.insert {
                it[messageId] = msg.idLong
                it[modId] = audit.user.idLong
                it[guildId] = event.guild.idLong
                it[targetId] = audit.targetIdLong
                it[caseId] = case
                it[type] = "KICK"
                it[reason] = audit.reason
            }
        }.execute()
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        asyncTransaction(pool) {
            val guild = Guilds.select { Guilds.id.eq(event.guild.idLong) }.firstOrNull()

            if (guild == null || !guild[Guilds.modlogs] || event.guild.getTextChannelById(guild[Guilds.modlogChannel]) == null)
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel])
            val audit = event.guild.auditLogs.type(ActionType.UNBAN).first { it.targetId == event.user.id }
            val case = modlogs.count() + 1

            val msg = modlogChannel.sendMessage("""
                **Unban** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

            Modlogs.insert {
                it[messageId] = msg.idLong
                it[modId] = audit.user.idLong
                it[guildId] = event.guild.idLong
                it[targetId] = audit.targetIdLong
                it[caseId] = case
                it[type] = "UNBAN"
                it[reason] = audit.reason
            }
        }.execute()
    }

    override fun onGuildBan(event: GuildBanEvent) {
        asyncTransaction(pool) {
            val guild = Guilds.select { Guilds.id.eq(event.guild.idLong) }.firstOrNull()

            if (guild == null || !guild[Guilds.modlogs] || event.guild.getTextChannelById(guild[Guilds.modlogChannel]) == null)
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel])
            val audit = event.guild.auditLogs.type(ActionType.BAN).first { it.targetId == event.user.id }
            val case = modlogs.count() + 1

            val msg = modlogChannel.sendMessage("""
                **Ban** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

            Modlogs.insert {
                it[messageId] = msg.idLong
                it[modId] = audit.user.idLong
                it[guildId] = event.guild.idLong
                it[targetId] = audit.targetIdLong
                it[caseId] = case
                it[type] = "BAN"
                it[reason] = audit.reason
            }
        }.execute()
    }

    companion object {
        @JvmStatic
        lateinit var client: Akatsuki
    }
}
*/