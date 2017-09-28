package me.noud02.akatsuki.bot.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = text("id").uniqueIndex().primaryKey()
    val username = varchar("username", 33)
    val discriminator = varchar("discriminator", 4)
    val lang = text("lang")
}