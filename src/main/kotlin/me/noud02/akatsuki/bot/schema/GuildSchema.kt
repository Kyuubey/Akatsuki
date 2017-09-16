package me.noud02.akatsuki.bot.schema

import org.jetbrains.exposed.sql.Table

object GuildSchema : Table() {
    val id = text("id").uniqueIndex().primaryKey()
    val name = varchar("name", 100)
    val lang = text("lang")

    // TODO: music commands then do this
    // val queue = pgArray<String>("queue", "text")
}