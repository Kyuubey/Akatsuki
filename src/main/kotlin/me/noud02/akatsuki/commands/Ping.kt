package me.noud02.akatsuki.commands

import khttp.get
import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Flag
import net.dv8tion.jda.core.entities.Member
import java.util.*

@Flag("meme", 'm', "this is a meme")
class Pong : Command() {
    override val name = "pong"
    override val desc = "Ping!"

    override fun run(ctx: Context) {
        if (ctx.flags.argMap.containsKey("meme")) {
            val res = get("https://www.reddit.com/r/dankmemes.json")
                    .jsonObject
                    .getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(Math.floor(Math.random() * 10).toInt())
                    .getJSONObject("data")
                    .getString("url")
            ctx.send(res)
        } else
            ctx.send("Ping!")
    }
}

@Argument("meme", "user", true)
class Ping : Command() {
    override val name = "ping"
    override val desc = "Pings a user if specified"

    init {
        addSubcommand(Pong())
    }

    override fun run(ctx: Context) {
        if (ctx.args["meme"] != null && ctx.args["meme"] is Member) {
            val user = ctx.args["meme"] as Member
            ctx.send(user.asMention)
        } else
            ctx.send("Pong!")
    }
}