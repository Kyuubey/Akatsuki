package me.noud02.akatsuki.commands

import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context

@Load
class Support : Command() {
    override val desc = "Get the invite for my support server!"

    override fun run(ctx: Context) = ctx.send("Join my support server for help! https://kyubey.moe/support")
}