package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.Command
import me.noud02.akatsuki.bot.Context

class Pong : Command() {
    override fun run(ctx: Context) {
        ctx.send("Ping!")
    }
}

class Ping : Command() {
    init {
        this.setSubcommand("pong", Pong())
    }

    override fun run(ctx: Context) {
        ctx.send("Pong!")
    }
}