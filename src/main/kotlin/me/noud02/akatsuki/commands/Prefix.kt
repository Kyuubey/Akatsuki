package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Perm
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
                ctx.send("Added prefix `${ctx.args["prefix"].toString()}`")
            } catch (e: Throwable) {
                ctx.send("Error while trying to update prefixes: ${e.message}")
            }
        }
    }
}

class Prefix : Command() {
    override val name = "prefix"
    override val desc = "Add, view or delete the guild's prefixes"

    init {
        addSubcommand(AddPrefix())
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