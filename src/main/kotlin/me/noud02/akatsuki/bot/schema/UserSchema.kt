package me.noud02.akatsuki.bot.schema

import org.jetbrains.exposed.sql.Table

object UserSchema : Table() {
    val id = text("id").uniqueIndex().primaryKey()
    val username = varchar("userame", 33)
    val lang = text("lang")
}