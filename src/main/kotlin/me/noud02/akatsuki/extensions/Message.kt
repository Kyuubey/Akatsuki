package me.noud02.akatsuki.extensions

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.db.schema.Logs
import org.jetbrains.exposed.sql.insert
import com.google.gson.Gson
import net.dv8tion.jda.core.entities.Message
import org.json.JSONObject

fun Message.log() {
    val gson = Gson()
    val embedArr = embeds.map { JSONObject(gson.toJson(it)) }.toTypedArray()

    asyncTransaction(Akatsuki.client.pool) {
        Logs.insert {
            it[id] = "test"
            it[messageId] = idLong
            it[content] = contentRaw
            it[attachments] = this@log.attachments.map { it.url }.toTypedArray()
            it[embeds] = embedArr
            it[authorId] = author.idLong
            it[authorName] = author.name
            it[authorDiscrim] = author.discriminator
            it[authorAvatar] = author.avatarUrl
            it[authorNick] = member?.nickname ?: ""
        }
    }.execute().get()
}