package me.noud02.akatsuki.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel

class GuildMusicManager(manager: AudioPlayerManager, val textChannel: TextChannel, val voiceChannel: VoiceChannel) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(player, this)
    val sendingHandler = AudioPlayerSendHandler(player)
    val voteSkip = mutableListOf<String>()

    init {
        player.addListener(scheduler)
        player.volume = 50
    }
}