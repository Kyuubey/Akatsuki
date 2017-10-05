package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.AsyncCommand
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import net.dv8tion.jda.core.entities.Member

@Load
@Argument("user", "user")
class Avatar : AsyncCommand() {
    override val name = "avatar"
    override val desc = "Get someones avatar"
    override val guildOnly = true

    override suspend fun asyncRun(ctx: Context) {
        ctx.send((ctx.args["user"] as Member).user.avatarUrl + "?size=2048")
    }
}