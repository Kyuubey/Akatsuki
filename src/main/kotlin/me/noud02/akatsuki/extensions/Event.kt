package me.noud02.akatsuki.extensions

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.db.schema.Logs
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.jetbrains.exposed.sql.insert
import com.google.gson.Gson

fun MessageReceivedEvent.log() {
    val gson = Gson()

    println(gson.toJson(message))

    asyncTransaction(Akatsuki.client.pool) {
        Logs.insert {
            it[id] = "test"
            it[messageId] = message.idLong
            it[content] = message.contentRaw
            it[attachments] = message.attachments.map { it.url }.toTypedArray()
            it[embeds] = arrayOf()
            it[authorId] = message.author.idLong
            it[authorName] = message.author.name
            it[authorDiscrim] = message.author.discriminator
            it[authorAvatar] = message.author.avatarUrl
            it[authorNick] = message.member?.nickname ?: ""
        }
    }.execute().get()
}