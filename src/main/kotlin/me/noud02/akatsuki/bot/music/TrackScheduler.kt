package me.noud02.akatsuki.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.timerTask

class TrackScheduler(private val player: AudioPlayer, private val manager: GuildMusicManager) : AudioEventAdapter() {
    val queue = LinkedBlockingQueue<AudioTrack>()

    fun add(track: AudioTrack) {
        if (!player.startTrack(track, true)) queue.offer(track)
    }

    fun next() {
        player.startTrack(queue.poll(), false)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val nextTrack = queue.peek()
        val embed = EmbedBuilder()

        embed.setTitle("Now playing: ${track.info.title}")
        embed.setColor(Color.CYAN)

        if (nextTrack != null)
            embed.setFooter("Next: ${nextTrack.info.title}", null)

        manager.textChannel.sendMessage(embed.build()).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            val nextTrack = queue.peek()
            val embed = EmbedBuilder()

            embed.setTitle("Track finished: ${track.info.title}")
            embed.setColor(Color.CYAN)

            if (nextTrack != null)
                embed.setFooter("Next: ${nextTrack.info.title}", null)
            /* TODO impl autoplay from yt
            if (manager.autoplay && track.info.uri.indexOf("youtube") > -1) {

            }*/ else
                MusicManager.inactivityScheduler.schedule(timerTask {
                    if (player.playingTrack != null || !manager.textChannel.guild.audioManager.isConnected) return@timerTask
                    manager.textChannel.sendMessage("Left voicechannel because of inactivity").queue()
                    MusicManager.leave(manager.textChannel.guild.id)
                }, 300000L)
            manager.textChannel.sendMessage(embed.build()).queue()
            next()
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        manager.textChannel.sendMessage("Error occurred while playing music: ${exception.message}").queue()
    }
}