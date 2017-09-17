package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context

@Argument("command", "string", true)
class Help : Command() {
    override val name = "help"
    override val desc = "Sends you help!"

    override fun run(ctx: Context) {
        if (ctx.args.contains("command"))
            return ctx.send(ctx.help(ctx.args["command"] as String))
    }
}