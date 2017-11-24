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

package me.noud02.akatsuki.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.utils.I18n
import me.noud02.akatsuki.music.MusicManager

@Load
@Argument("url|query", "string")
class Play : Command() {
    override val desc = "Play music!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")
        
        if (!ctx.guild!!.audioManager.isConnected)
            return ctx.send(I18n.parse(ctx.lang.getString("join_voice_channel_fail"), mapOf("username" to ctx.author.name)))

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