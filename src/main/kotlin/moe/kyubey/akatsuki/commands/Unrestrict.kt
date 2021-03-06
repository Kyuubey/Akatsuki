/*
 *   Copyright (c) 2017-2019 Yui
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

package moe.kyubey.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import moe.kyubey.akatsuki.Akatsuki
import moe.kyubey.akatsuki.EventListener
import moe.kyubey.akatsuki.annotations.Argument
import moe.kyubey.akatsuki.annotations.Arguments
import moe.kyubey.akatsuki.annotations.Load
import moe.kyubey.akatsuki.annotations.Perm
import moe.kyubey.akatsuki.db.schema.Restrictions
import moe.kyubey.akatsuki.entities.Command
import moe.kyubey.akatsuki.entities.Context
import moe.kyubey.akatsuki.utils.I18n
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

@Arguments(
        Argument("command", "string"),
        Argument("user", "user"),
        Argument("reason", "string", true)
)
class UnrestrictGlobally : Command() {
    override val desc = "Unrestrict usage of commands globally."
    override val ownerOnly = true
    override val guildOnly = true

    override fun run(ctx: Context) {
        val cmd = ctx.args["command"] as String
        val mem = ctx.args["user"] as Member

        if (cmd !in EventListener.cmdHandler.commands && cmd != "all") {
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("command_not_found"),
                            mapOf("username" to ctx.author.name)
                    )
            )
        }

        asyncTransaction(Akatsuki.pool) {
            Restrictions.deleteWhere {
                Restrictions.global.eq(true) and Restrictions.userId.eq(mem.user.idLong) and Restrictions.command.eq(cmd)
            }

            ctx.send("Unrestricted global usage of command $cmd for user ${mem.user.name}#${mem.user.discriminator}!") // TODO translation
        }.execute()
    }
}

@Load
@Perm(Permission.MANAGE_SERVER)
@Arguments(
        Argument("command", "string"),
        Argument("user", "user", true)
)
class Unrestrict : Command() {
    override val desc = "Unrestrict usage of commands."
    override val guildOnly = true

    init {
        addSubcommand(UnrestrictGlobally(), "global")
    }

    override fun run(ctx: Context) {
        val cmd = ctx.args["command"] as String

        if (cmd !in EventListener.cmdHandler.commands && cmd != "all") {
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("command_not_found"),
                            mapOf("username" to ctx.author.name)
                    )
            )
        }

        asyncTransaction(Akatsuki.pool) {
            if ("user" in ctx.args) {
                val mem = ctx.args["user"] as Member

                Restrictions.deleteWhere {
                    Restrictions.guildId.eq(ctx.guild!!.idLong) and Restrictions.command.eq(cmd) and Restrictions.userId.eq(mem.user.idLong)
                }

                ctx.send("Unrestricted usage of command $cmd for user ${mem.user.name}#${mem.user.discriminator}!") // TODO translation
            } else {
                Restrictions.deleteWhere {
                    Restrictions.guildId.eq(ctx.guild!!.idLong) and Restrictions.command.eq(cmd) and Restrictions.everyone.eq(true)
                }

                ctx.send("Unrestricted usage of command $cmd!") // TODO translation
            }
        }.execute()
    }
}