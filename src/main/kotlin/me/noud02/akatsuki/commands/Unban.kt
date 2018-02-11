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

import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Arguments
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.entities.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.exceptions.PermissionException

@Load
@Perm(Permission.BAN_MEMBERS)
@Arguments(
        Argument("user", "string"),
        Argument("reason", "string", true)
)
class Unban : Command() {
    override val desc = "Unban members from the guild"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as String

        ctx.guild!!.controller
                .unban(user)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send("Unbanned <@$user>")
                }) { err ->
                    if (err is PermissionException)
                        ctx.send("I couldn't unban <@$user> because I'm missing the '${err.permission}' permission!")
                    else
                        ctx.send("I couldn't ban <@$user> because of an unknown error: ${err.message}")
                }
    }
}