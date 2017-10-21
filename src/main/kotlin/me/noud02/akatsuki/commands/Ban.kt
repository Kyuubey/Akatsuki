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

import me.noud02.akatsuki.bot.entities.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.exceptions.PermissionException

@Load
@Perm(Permission.BAN_MEMBERS)
@Arguments([
    Argument("user", "user"),
    Argument("reason", "string", true)
])
class Ban : Command() {
    override val name = "ban"
    override val desc = "Ban members from the guild"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member

        if (!ctx.member!!.canInteract(user))
            return ctx.send("You can't ban that user!")

        if (!ctx.selfMember!!.canInteract(user))
            return ctx.send("I can't ban that user!")

        ctx.guild!!.controller.ban(user, 24).queue({ ctx.send("Banned ${user.user.name}") }, { err -> run {
            if (err is PermissionException)
                ctx.send("I couldn't ban ${user.user.name} because I'm missing the '${err.permission}' permission!")
            else
                ctx.send("I couldn't ban ${user.user.name} because of an unknown error: ${err.message}")
        }})
    }
}