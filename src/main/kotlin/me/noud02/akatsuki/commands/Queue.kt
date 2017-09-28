package me.noud02.akatsuki.commands

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.entities.Perm
import me.noud02.akatsuki.bot.music.MusicManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.awt.Color

@Perm(Permission.MANAGE_SERVER)
class Clear : Command() {
    override val name = "clear"
    override val desc = "Clear the queue!"

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")

        manager.scheduler.queue.clear()
        ctx.send("Cleared the queue!")
    }
}

@Load
class Queue : Command() {
    override val name = "queue"
    override val desc = "View the queue!"
    override val guildOnly = true

    init {
        addSubcommand(Clear())
    }

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")
        val queue = manager.scheduler.queue

        val formatted = queue.mapIndexed { i: Int, audioTrack: AudioTrack -> "${i + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})" }.joinToString("\n")
        val embed = EmbedBuilder()

        embed.setColor(Color.CYAN)
        embed.setTitle("${ctx.lang.getString("queue")}:")
        val desc = embed.descriptionBuilder.append(formatted)
        embed.setDescription(desc)

        ctx.send(embed.build())
    }
}