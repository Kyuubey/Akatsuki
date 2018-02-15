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

import me.aurieh.ares.core.entities.EventWaiter
import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.db.DatabaseWrapper
import me.noud02.akatsuki.db.schema.Contracts
import me.noud02.akatsuki.db.schema.Guilds
import me.noud02.akatsuki.db.schema.Modlogs
import me.noud02.akatsuki.extensions.addStar
import me.noud02.akatsuki.extensions.log
import me.noud02.akatsuki.extensions.removeStar
import me.noud02.akatsuki.music.MusicManager
import me.noud02.akatsuki.utils.Logger
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.awt.Color
import kotlin.math.exp
import kotlin.reflect.jvm.jvmName

class EventListener : ListenerAdapter() {
    private val logger = Logger(this::class.jvmName)

    val cmdHandler = CommandHandler()
    val snipes = mutableMapOf<Long, Long>()
    val waiter = EventWaiter()
    val pool = Akatsuki.instance.pool

    init {
        EventListener.instance = this
    }

    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.guild != null) {
            val stored = DatabaseWrapper.getGuildSafe(event.guild).get()
            if (stored.logs)
                event.message.log()

            asyncTransaction(Akatsuki.instance.pool) {
                val contract = Contracts.select { Contracts.userId.eq(event.author.idLong) }.firstOrNull() ?: return@asyncTransaction
                val curLevel = contract[Contracts.level]
                val xp = contract[Contracts.experience]

                val xpNeeded = curLevel.toFloat() * 500f * (curLevel.toFloat() / 3f)

                if (xp >= xpNeeded) {
                    Contracts.update({
                        Contracts.userId.eq(event.author.idLong)
                    }) {
                        it[level] = curLevel + 1
                        it[experience] = 0
                        it[balance] = contract[Contracts.balance] + 2500
                    }

                    if (stored.levelMessages)
                        event.channel.sendMessage(EmbedBuilder().apply {
                            setTitle("${event.author.name}, you are now rank ${curLevel+1}!")
                            setColor(Color.CYAN)
                            descriptionBuilder.append("+2500$\n")

                            // TODO add random items on levelup
                        }.build()).queue()
                }
            }.execute()
        }

        if (event.author.isBot)
            return

        try {
            cmdHandler.handleMessage(event)
        } catch (e: Exception) {
            logger.error("Error while trying to handle message", e)
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.guild != null && DatabaseWrapper.getGuildSafe(event.guild).get().logs)
            DatabaseWrapper.logEvent(event)
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (event.guild != null && DatabaseWrapper.getGuildSafe(event.guild).get().logs)
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
            val guild = DatabaseWrapper.getGuildSafe(event.guild).get()

            if (!guild.starboard)
                return

            val msg = event.channel.getMessageById(event.messageId).complete()

            event.guild.addStar(msg, event.user)
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (event.guild != null && event.reaction.reactionEmote.name == "\u2b50") {
            val guild = DatabaseWrapper.getGuildSafe(event.guild).get()

            if (!guild.starboard)
                return

            val msg = event.channel.getMessageById(event.messageId).complete()

            event.guild.removeStar(msg, event.user)
        }
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.member.user.id == event.jda.selfUser.id
                || !event.channelLeft.members.any { it.user.id == event.jda.selfUser.id }
                || event.channelLeft.members.size > 1)
            return

        MusicManager.leave(event.guild.id)
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val storedGuild = DatabaseWrapper.getGuildSafe(event.guild).get()
        val channel = event.guild.getTextChannelById(storedGuild.welcomeChannel ?: return) ?: return

        if (storedGuild.welcome && storedGuild.welcomeMessage.isNotBlank())
            channel.sendMessage(
                    storedGuild.welcomeMessage
                            .replace("%USER%", event.user.asMention)
                            .replace("%USERNAME%", event.user.name)
                            .replace("%SERVER%", event.guild.name)
                            .replace("%MEMBERNUM%", (event.guild.members.indexOf(event.member) + 1).toString())
            ).queue()

    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        val storedGuild = DatabaseWrapper.getGuildSafe(event.guild).get()
        val channel = event.guild.getTextChannelById(storedGuild.welcomeChannel ?: return) ?: return

        if (storedGuild.welcome && storedGuild.leaveMessage.isNotBlank())
            channel.sendMessage(
                    storedGuild.leaveMessage
                            .replace("%USER%", event.user.asMention)
                            .replace("%USERNAME%", event.user.name)
                            .replace("%SERVER%", event.guild.name)
            ).queue()

        asyncTransaction(pool) {
            val guild = Guilds.select { Guilds.id.eq(event.guild.idLong) }.firstOrNull()

            if (guild == null || !guild[Guilds.modlogs])
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel] ?: return@asyncTransaction) ?: return@asyncTransaction
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

            if (guild == null || !guild[Guilds.modlogs])
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel] ?: return@asyncTransaction) ?: return@asyncTransaction
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

            if (guild == null || !guild[Guilds.modlogs])
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel] ?: return@asyncTransaction) ?: return@asyncTransaction
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

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {
        asyncTransaction(pool) {
            val guild = Guilds.select { Guilds.id.eq(event.guild.idLong) }.firstOrNull()

            if (guild == null
                    || !guild[Guilds.modlogs]
                    || !event.roles.contains(event.guild.getRoleById(guild[Guilds.mutedRole] ?: return@asyncTransaction) ?: return@asyncTransaction))
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel] ?: return@asyncTransaction) ?: return@asyncTransaction
            val audit = event.guild.auditLogs.type(ActionType.MEMBER_ROLE_UPDATE).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction
            val case = modlogs.count() + 1

            val msg = modlogChannel.sendMessage("""
                **Mute** | Case $case
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
                it[type] = "MUTE"
                it[reason] = audit.reason
            }
        }.execute()
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {
        asyncTransaction(pool) {
            val guild = Guilds.select { Guilds.id.eq(event.guild.idLong) }.firstOrNull()

            if (guild == null
                    || !guild[Guilds.modlogs]
                    || !event.roles.contains(event.guild.getRoleById(guild[Guilds.mutedRole] ?: return@asyncTransaction) ?: return@asyncTransaction))
                return@asyncTransaction

            val modlogs = Modlogs.select { Modlogs.guildId.eq(event.guild.idLong) }
            val modlogChannel = event.guild.getTextChannelById(guild[Guilds.modlogChannel] ?: return@asyncTransaction) ?: return@asyncTransaction
            val audit = event.guild.auditLogs.type(ActionType.MEMBER_ROLE_UPDATE).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction
            val case = modlogs.count() + 1

            val msg = modlogChannel.sendMessage("""
                **Unmute** | Case $case
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
                it[type] = "UNMUTE"
                it[reason] = audit.reason
            }
        }.execute()
    }

    companion object {
        @JvmStatic
        lateinit var instance: EventListener
    }
}