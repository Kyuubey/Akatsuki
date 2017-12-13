package me.noud02.akatsuki.commands

import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.extensions.log

@Load
class Logs : Command() {
    override fun run(ctx: Context) {
        ctx.event.log()
    }
}