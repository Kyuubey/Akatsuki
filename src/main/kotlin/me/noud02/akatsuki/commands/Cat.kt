package me.noud02.akatsuki.commands

import khttp.get
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context

class Cat : Command() {
    override val name = "cat"
    override val desc = "Get a random cat"

    override fun run(ctx: Context) {
        val cat = get("https://random.cat/meow").jsonObject.getString("file")
        ctx.send(cat)
    }
}