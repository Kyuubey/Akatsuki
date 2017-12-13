package me.noud02.akatsuki.db.schema

import me.aurieh.ares.exposed.pg.jsonbArray
import me.aurieh.ares.exposed.pg.pgArray
import org.jetbrains.exposed.sql.Table
import org.json.JSONObject

object Logs : Table() {
    val id = varchar("id", 100)
            .uniqueIndex()
            .primaryKey()

    // Message
    val messageId = long("messageId")
    val content = varchar("content", 2000)
    val attachments = pgArray<String>("attachments", "text")
    val embeds = jsonbArray<JSONObject>("embeds")

    // Author
    val authorId = long("authorId")
    val authorName = varchar("authorName", 33)
    val authorDiscrim = varchar("authorDiscrim", 4)
    val authorAvatar = text("authorAvatar")
    val authorNick = varchar("authorNick", 33)
}