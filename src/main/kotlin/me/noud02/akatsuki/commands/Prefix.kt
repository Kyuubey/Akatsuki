/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.db.schema.Guilds
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class AddPrefix : Command() {
    override val guildOnly = true
    override val desc = "Add a prefix"

    override fun run(ctx: Context) {
        asyncTransaction(Akatsuki.instance.pool) {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild!!.idLong)
            }.first()

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    it[prefixes] = guild[Guilds.prefixes] + ctx.args["prefix"].toString()
                }
                ctx.send("Added prefix `${ctx.args["prefix"]}`")
            } catch (e: Throwable) {
                ctx.send("Error while trying to update prefixes: ${e.message}")
            }
        }.execute()
    }
}

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class RemPrefix : Command() {
    override val guildOnly = true
    override val desc = "Remove a prefix"

    override fun run(ctx: Context) {
        asyncTransaction(Akatsuki.instance.pool) {
            val guild = Guilds.select { // TODO add guild from db to Context class
                Guilds.id.eq(ctx.guild!!.idLong)
            }.first()

            if (guild[Guilds.prefixes].isEmpty())
                return@asyncTransaction ctx.send("No prefixes to remove!") // TODO add translations for this

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    val list = guild[Guilds.prefixes].toMutableList()
                    list.remove(ctx.args["prefix"])
                    it[prefixes] = list.toTypedArray()
                }
                ctx.send("Removed prefix `${ctx.args["prefix"]}`")
            } catch (e: Throwable) {
                ctx.send("Error while trying to update prefixes: ${e.message}")
            }
        }.execute()
    }
}

@Load
class Prefix : Command() {
    override val guildOnly = true
    override val desc = "Add, view or delete the guild's prefixes"

    init {
        addSubcommand(AddPrefix(), "add")
        addSubcommand(RemPrefix(), "remove")
    }

    override fun run(ctx: Context)
            = ctx.send("Current prefixes: ${
    if (ctx.storedGuild!!.prefixes.isEmpty())
        "none"
    else
        ctx.storedGuild.prefixes.joinToString(", ")}") // TODO add translations
}