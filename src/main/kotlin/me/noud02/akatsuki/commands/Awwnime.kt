package me.noud02.akatsuki.commands

import khttp.get
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load

@Load
class Awwnime : Command() {
    override val name = "awwnime"
    override val desc = "Get a random post from /r/awwnime"

    override fun run(ctx: Context) {
        val res = get("https://www.reddit.com/r/awwnime.json")
                .jsonObject
                .getJSONObject("data")
                .getJSONArray("children")

        val post = res
                .getJSONObject(Math.floor(Math.random() * res.count()).toInt())

        ctx.send(
                post
                .getJSONObject("data")
                .getString("url")
        )
    }
}