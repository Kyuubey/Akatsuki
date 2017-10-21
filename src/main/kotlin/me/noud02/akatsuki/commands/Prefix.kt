/*
 *  Copyright (c) 2017 Noud Kerver
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.bot.entities.*
import me.noud02.akatsuki.bot.schema.Guilds
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class AddPrefix : AsyncCommand() {
    override val name = "add"
    override val desc = "Add a prefix"

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
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
        }.await()
    }
}

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class RemPrefix : AsyncCommand() {
    override val name = "rem"
    override val desc = "Remove a prefix"

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
            val guild = Guilds.select { // TODO add guild from db to Context class
                Guilds.id.eq(ctx.guild?.id)
            }.first()

            if (guild[Guilds.prefixes].size == 1)
                return@asyncTransaction ctx.send("You need to have at least 1 custom prefix!") // TODO add translations for this

            if (guild[Guilds.prefixes].isEmpty())
                return@asyncTransaction ctx.send("No prefixes to remove!") // TODO add translations for this

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild?.id)
                }) {
                    val list = guild[Guilds.prefixes].toMutableList()
                    list.remove(ctx.args["prefix"])
                    it[prefixes] = list.toTypedArray()
                }
                ctx.send("Removed prefix `${ctx.args["prefix"]}`")
            } catch (e: Throwable) {
                ctx.send("Error while trying to update prefixes: ${e.message}")
            }
        }.await()
    }
}

@Load
class Prefix : AsyncCommand() {
    override val name = "prefix"
    override val desc = "Add, view or delete the guild's prefixes"

    init {
        addSubcommand(AddPrefix())
        addSubcommand(RemPrefix())
    }

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild?.id)
            }.first()

            ctx.send("Current prefixes: ${guild[Guilds.prefixes].joinToString(", ")}") // TODO change this translation to allow more prefixes
        }.await()
    }
}