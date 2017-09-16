package me.noud02.akatsuki.bot

import me.noud02.akatsuki.bot.schema.GuildSchema
import me.noud02.akatsuki.bot.schema.UserSchema
import net.dv8tion.jda.core.entities.Guild
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert

class Database {
    init {
        transaction {
            create(GuildSchema, UserSchema)
        }
    }
}