/*
 *  Copyright (c) 2017 Noud Kerver
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

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