package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import net.dv8tion.jda.core.entities.Member

class Pong : Command() {
    override val name = "pong"

    override fun run(ctx: Context) {
       ctx.send("Ping!")
    }
}

class Ping : Command() {
    override val name = "ping"

    init {
        addSubcommand(Pong())
        setArgument("meme", "user")
    }

    override fun run(ctx: Context) {
        if (ctx.args["meme"] != null && ctx.args["meme"] is Member) {
            val user = ctx.args["meme"] as Member
            ctx.send(user.asMention)
        } else
            ctx.send("Pong!")
    }
}