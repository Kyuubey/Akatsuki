package me.noud02.akatsuki.bot.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import me.noud02.akatsuki.bot.entities.Context
import net.dv8tion.jda.core.entities.VoiceChannel
import org.slf4j.LoggerFactory
import java.util.*

object MusicManager {
    private val loggr = LoggerFactory.getLogger(this::class.java)

    val playerManager = DefaultAudioPlayerManager()
    val musicManagers = mutableMapOf<String, GuildMusicManager>()
    val inactivityScheduler = Timer(true)

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun join(ctx: Context): GuildMusicManager {
        loggr.info("New voice connection in guild ${ctx.guild!!.name}!")
        val manager = GuildMusicManager(playerManager, ctx.event.textChannel, ctx.member!!.voiceState.channel as VoiceChannel)
        musicManagers[ctx.guild.id] = manager
        ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
        ctx.guild.audioManager.sendingHandler = manager.sendingHandler
        return manager
    }

    fun leave(guild: String): Boolean {
        loggr.info("Voice connection ended in guild with id $guild!")
        val manager = musicManagers[guild] ?: return false
        manager.player.stopTrack()
        manager.scheduler.queue.clear()
        manager.voiceChannel.guild.audioManager.closeAudioConnection()
        musicManagers.remove(guild)
        return true
    }
}