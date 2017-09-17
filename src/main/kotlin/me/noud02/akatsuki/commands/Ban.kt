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