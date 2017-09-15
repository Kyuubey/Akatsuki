package akatsuki.commands

import akatsuki.Command
import akatsuki.Context

class Pong : Command() {
    fun run(ctx: Context) {
        ctx.send("Pong!")
    }
}

class Ping : Command() {
    init {
        this.setSubcommand("pong", Pong())
    }

    fun run(ctx: Context) {
        ctx.send("Pong!")
    }
}