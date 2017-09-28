package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Perm
import me.noud02.akatsuki.bot.music.MusicManager
import net.dv8tion.jda.core.Permission

@Perm(Permission.MANAGE_SERVER)
class Pause : Command() {
    override val name = "pause"
    override val desc = "Pause the current song!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")
        val state = manager.player.isPaused

        manager.player.isPaused = !state

        if (!state)
            ctx.send(ctx.lang.getString("paused"))
        else
            ctx.send("Resumed the music!") // TODO add translations for "resumed"
    }
}