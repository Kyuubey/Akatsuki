package akatsuki.commands

import akatsuki.Command
import akatsuki.Context

class Pong : Command() {
    override fun run(ctx: Context) {
        ctx.send("Pong!")
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