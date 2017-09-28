package me.noud02.akatsuki.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.i18n
import me.noud02.akatsuki.bot.music.MusicManager

@Load
@Argument("url|query", "string")
class Play : Command() {
    override val name = "play"
    override val desc = "Play music!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")

        if (!ctx.guild!!.audioManager.isConnected)
            return ctx.send(i18n.parse(ctx.lang.getString("join_voice_channel_fail"), mapOf("username" to ctx.author.name)))

        val search = ctx.rawArgs.joinToString(" ")

        // TODO change translations from "download" to "add"

        MusicManager.playerManager.loadItemOrdered(manager, search, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) = ctx.send("Failed to add song to queue: ${exception.message}")
            override fun noMatches() {
                MusicManager.playerManager.loadItem("ytsearch:$search", object : AudioLoadResultHandler {
                    override fun loadFailed(exception: FriendlyException) = ctx.send("Failed to add song to queue: ${exception.message}")
                    override fun noMatches() = ctx.send("Could not find that song!")
                    override fun trackLoaded(track: AudioTrack) {
                        manager.scheduler.add(track)
                        ctx.send("Added ${track.info.title} to the queue!")
                    }
                    override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.tracks.first())
                })
            }
            override fun trackLoaded(track: AudioTrack) {
                manager.scheduler.add(track)
                ctx.send("Added ${track.info.title} to the queue!")
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {
                for (track in playlist.tracks) {
                    manager.scheduler.add(track)
                }
                ctx.send("Added ${playlist.tracks.size} tracks from playlist ${playlist.name} to the queue!")
            }
        })
    }
}