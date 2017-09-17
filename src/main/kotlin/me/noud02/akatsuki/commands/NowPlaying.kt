package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.music.MusicManager
import net.dv8tion.jda.core.EmbedBuilder

@Load
class NowPlaying : Command() {
    override val name = "np"
    override val desc = "Get the current song"

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")
        val embed = EmbedBuilder()

        embed.setAuthor("Now playing", null, null)
        embed.setTitle(manager.player.playingTrack.info.title)
        if (manager.scheduler.queue.isNotEmpty())
            embed.setFooter("Next: ${manager.scheduler.queue.peek().info.title}", null)

        ctx.send(embed.build())
    }
}