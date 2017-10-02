package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Alias
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.music.MusicManager
import net.dv8tion.jda.core.EmbedBuilder

@Load
@Alias("np")
class NowPlaying : Command() {
    override val name = "nowplaying"
    override val desc = "Get the current song"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!") // TODO add translations for "not connected"
        val embed = EmbedBuilder()

        embed.setAuthor(ctx.lang.getString("now_playing"), null, null)
        embed.setTitle(manager.player.playingTrack.info.title)
        if (manager.scheduler.queue.isNotEmpty())
            embed.setFooter("Next: ${manager.scheduler.queue.peek().info.title}", null) // TODO add translations for "next"

        ctx.send(embed.build())
    }
}