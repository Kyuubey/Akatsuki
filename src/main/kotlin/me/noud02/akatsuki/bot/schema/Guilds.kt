package me.noud02.akatsuki.bot.schema

import me.aurieh.ares.exposed.pg.pgArray
import org.jetbrains.exposed.sql.Table

object Guilds : Table() {
    val id = text("id").uniqueIndex().primaryKey()
    val name = varchar("name", 100)
    val lang = text("lang")
    val prefixes = pgArray<String>("prefixes", "varchar")
    val forceLang = bool("forceLang")
}