package me.noud02.akatsuki.commands

import khttp.get
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context

class Dog: Command() {
    override val name = "dog"
    override val desc = "Get a random dog"

    override fun run(ctx: Context) {
        val dog = get("https://random.dog/woof").text
        ctx.send("https://random.dog/$dog")
    }
}