package me.noud02.akatsuki.commands

import khttp.get
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load

@Load
class Birb : Command() {
    override val name = "birb"
    override val desc = "Get a random birb"

    override fun run(ctx: Context) {
        val birb = get("https://random.birb.pw/tweet").text
        ctx.send("https://random.birb.pw/img/$birb")
    }
}