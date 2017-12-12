package me.noud02.akatsuki.db.schema

import me.aurieh.ares.exposed.pg.pgArray
import org.jetbrains.exposed.sql.Table

object Starboard : Table() {
    val messageId = long("messageId")
            .uniqueIndex()
            .primaryKey()
    val guildId = long("guildId")
    val channelId = long("channelId")
    val starId = long("starId")
    val stargazers = pgArray<Long>("stargazers", "BIGINT")
    val content = varchar("content", 2000)
    val attachments = pgArray<String>("attachments", "text")
}