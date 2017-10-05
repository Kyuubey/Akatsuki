package me.noud02.akatsuki.bot.utils

import me.aurieh.ares.core.entities.EventWaiter
import me.noud02.akatsuki.bot.entities.PickerItem
import me.noud02.akatsuki.bot.extensions.await
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.awt.Color
import java.util.concurrent.CompletableFuture

class ItemPicker(private val waiter: EventWaiter, private val user: Member, private val guild: Guild, private val timeout: Long = 60000) {
    private var index = 0
    private val embeds = mutableListOf<MessageEmbed>()

    private val rightEmote = "\u27A1"
    private val leftEmote = "\u2B05"
    private val confirmEmote = "\u2705"
    private val cancelEmote = "\u23F9"

    var color = Color.CYAN
    val items = mutableListOf<PickerItem>()

    suspend fun build(msg: Message): CompletableFuture<PickerItem> = build(msg.channel)

    suspend fun build(channel: MessageChannel): CompletableFuture<PickerItem> {
        return if (user.hasPermission(Permission.MESSAGE_ADD_REACTION))
            buildReactions(channel)
        else
            buildInput(channel)
    }

    private suspend fun buildReactions(channel: MessageChannel): CompletableFuture<PickerItem> {
        val fut = CompletableFuture<PickerItem>()

        for (item in items) {
            val embed = EmbedBuilder()
                    .setColor(if (item.color != null) item.color else color)
                    .setFooter("${if (item.footer.isNotBlank()) "${item.footer} | " else ""}Page ${items.indexOf(item) + 1}/${items.size}", null)

            if (item.author.isNotBlank())
                embed.setAuthor(item.author, null, null)

            if (item.title.isNotBlank())
                embed.setTitle(item.title)

            if (item.description.isNotBlank())
                embed.setDescription(embed.descriptionBuilder.append(item.description))

            if (item.thumbnail.isNotBlank())
                embed.setThumbnail(item.thumbnail)

            if (item.image.isNotBlank())
                embed.setImage(item.image)

            embeds.add(embed.build())
        }

        val msg = channel.sendMessage(embeds[index]).await()

        waiter.await<MessageReactionAddEvent>(30, timeout) {
            if (it.messageId == msg.id && it.user.id == user.user.id) {
                when (it.reaction.emote.name) {
                    rightEmote -> {
                        it.reaction.removeReaction(it.user).queue()
                        if (index - 1 >= 0) {
                            index--
                        }
                    }

                    leftEmote -> {
                        it.reaction.removeReaction(it.user).queue()
                        if (index + 1 <= items.size) {
                            index++
                        }
                    }

                    confirmEmote -> {
                        msg.delete().queue()
                        fut.complete(items[index])
                    }

                    cancelEmote -> {
                        msg.delete().queue()
                    }
                }
                true
            } else
                false
        }

        return fut
    }

    private suspend fun buildInput(channel: MessageChannel): CompletableFuture<PickerItem> {
        val fut = CompletableFuture<PickerItem>()
        val msg = channel.sendMessage("Please choose an item from the list by sending its number:\n```\n${items.mapIndexed { i, item -> " ${i + 1}. ${item.title}" }.joinToString("\n")}```").await()

        waiter.await<MessageReceivedEvent>(1, timeout) {
            if (it.channel.id == msg.channel.id && it.author.id == user.user.id) {
                if (it.message.rawContent.toIntOrNull() == null)
                    msg.channel.sendMessage("Invalid number").queue()
                else if (it.message.rawContent.toInt() - 1 > items.size || it.message.rawContent.toInt() - 1 < 0)
                    msg.channel.sendMessage("Number out of bounds!")
                else {
                    index = it.message.rawContent.toInt() - 1
                    msg.delete().queue()
                    fut.complete(items[index])
                }
                true
            } else
                false
        }

        return fut
    }

}