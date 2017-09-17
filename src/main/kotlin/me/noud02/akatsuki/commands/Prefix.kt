package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.*
import me.noud02.akatsuki.bot.schema.Guilds
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class AddPrefix : Command() {
    override val name = "add"
    override val desc = "Add a prefix"

    override fun run(ctx: Context) {
        transaction {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild?.id)
            }.first()

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild?.id)
                }) {
                    it[prefixes] = guild[Guilds.prefixes].plus(ctx.args["prefix"].toString())
                }
                ctx.send("Added prefix `${ctx.args["prefix"]}`")
            } catch (e: Throwable) {
                ctx.send("Error while trying to update prefixes: ${e.message}")
            }
        }
    }
}

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class RemPrefix : Command() {
    override val name = "rem"
    override val desc = "Remove a prefix"

    override fun run(ctx: Context) {
        transaction {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild?.id)
            }.first()

            if (guild[Guilds.prefixes].size == 1) {
                ctx.send("You need to have at least 1 custom prefix!")
                return@transaction
            }

            if (guild[Guilds.prefixes].isEmpty()) {
                ctx.send("No prefixes to remove!")
                return@transaction
            }

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild?.id)
                }) {
                    val list = mutableListOf(*guild[Guilds.prefixes])
                    list.remove(ctx.args["prefix"])
                    it[prefixes] = list.toTypedArray()
                }
                ctx.send("Removed prefix `${ctx.args["prefix"]}`")
            } catch (e: Throwable) {
                ctx.send("Error while trying to update prefixes: ${e.message}")
            }
        }
    }
}

@Load
class Prefix : Command() {
    override val name = "prefix"
    override val desc = "Add, view or delete the guild's prefixes"

    init {
        addSubcommand(AddPrefix())
        addSubcommand(RemPrefix())
    }

    override fun run(ctx: Context) {
        transaction {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild?.id)
            }.first()

            ctx.send("Current prefixes: ${guild[Guilds.prefixes].joinToString(", ")}")
        }
    }
}