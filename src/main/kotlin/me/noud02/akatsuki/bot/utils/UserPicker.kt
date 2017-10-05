package me.noud02.akatsuki.bot.utils

import me.aurieh.ares.core.entities.EventWaiter
import me.noud02.akatsuki.bot.extensions.await
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.concurrent.CompletableFuture

class UserPicker(private val waiter: EventWaiter, private val user: Member, private var users: List<Member>, private val guild: Guild, private val timeout: Long = 60000) {
    private var index = 0
    private val text get() = "Please select a user:\n```asciidoc\n${users.mapIndexed { i, member -> if (i == index) "*${i + 1}. ${member.user.name}#${member.user.discriminator} *" else " ${i + 1}. ${member.user.name}#${member.user.discriminator}" }.joinToString("\n")}```"
    private val inputText = "Please select a user by sending their number:\n```asciidoc\n${users.mapIndexed { i, member -> " ${i + 1}. ${member.user.name}#${member.user.discriminator}" }.joinToString("\n")}```"

    private val upEmote = "\u2B06"
    private val downEmote = "\u2B07"
    private val confirmEmote = "\u2705"
    private val cancelEmote = "\u23F9"

    init {
        users = users.subList(0, 5)
    }

    suspend fun build(msg: Message): CompletableFuture<Member> = build(msg.channel)

    suspend fun build(channel: MessageChannel): CompletableFuture<Member> {
        return if (guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION))
            buildReactions(channel)
        else
            buildInput(channel)
    }

    private suspend fun buildReactions(channel: MessageChannel): CompletableFuture<Member> {
        val msg = channel.sendMessage(text).await()
        val fut = CompletableFuture<Member>()

        msg.addReaction(upEmote).await()
        msg.addReaction(confirmEmote).await()
        msg.addReaction(cancelEmote).await()
        msg.addReaction(downEmote).await()

        waiter.await<MessageReactionAddEvent>(20, timeout) {
            if (it.messageId == msg.id && it.user.id == user.user.id) {
                when (it.reaction.emote.name) {
                    upEmote -> {
                        it.reaction.removeReaction(it.user).queue()
                        if (index - 1 >= 0) {
                            index--
                            msg.editMessage(text).queue()
                        }
                    }

                    downEmote -> {
                        it.reaction.removeReaction(it.user).queue()
                        if (index + 1 <= users.size) {
                            index++
                            msg.editMessage(text).queue()
                        }
                    }

                    cancelEmote -> {
                        msg.delete().queue()
                    }

                    confirmEmote -> {
                        msg.delete().queue()
                        fut.complete(users[index])
                    }
                }
                true
            } else
                false
        }

        return fut
    }

    private suspend fun buildInput(channel: MessageChannel): CompletableFuture<Member> {
        val msg = channel.sendMessage(inputText).await()
        val fut = CompletableFuture<Member>()

        waiter.await<MessageReceivedEvent>(1, timeout) {
            if (it.channel.id == msg.channel.id && it.author.id == user.user.id) {
                if (it.message.rawContent.toIntOrNull() == null)
                    msg.channel.sendMessage("Invalid number").queue()
                else if (it.message.rawContent.toInt() - 1 > users.size || it.message.rawContent.toInt() - 1 < 0)
                    msg.channel.sendMessage("Number out of bounds!")
                else {
                    index = it.message.rawContent.toInt() - 1
                    msg.delete().queue()
                    fut.complete(users[index])
                }
                true
            } else
                false
        }

        return fut
    }
}