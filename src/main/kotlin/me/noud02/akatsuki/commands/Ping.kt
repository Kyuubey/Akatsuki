package me.noud02.akatsuki.commands

import khttp.get
import me.noud02.akatsuki.bot.entities.*
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import java.time.temporal.ChronoUnit

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

@Load
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
        } else {
            ctx.event.channel.sendMessage("Pong!").queue({ message: Message ->
                message.editMessage("Pong! `${ctx.msg.creationTime.until(message.creationTime, ChronoUnit.MILLIS)}ms`").queue()
            })
        }
    }
}