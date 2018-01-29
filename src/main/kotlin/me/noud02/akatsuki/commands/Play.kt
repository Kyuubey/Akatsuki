/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.EventListener
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.PickerItem
import me.noud02.akatsuki.music.GuildMusicManager
import me.noud02.akatsuki.utils.I18n
import me.noud02.akatsuki.music.MusicManager
import me.noud02.akatsuki.utils.ItemPicker
import net.dv8tion.jda.core.audio.hooks.ConnectionListener
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color

@Load
@Argument("url|query", "string")
class Play : Command() {
    override val desc = "Play music!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel())
            return ctx.send(I18n.parse(ctx.lang.getString("join_voice_channel_fail"), mapOf("username" to ctx.author.name)))

        if (MusicManager.musicManagers[ctx.guild!!.id] == null) {
            val manager = MusicManager.join(ctx)
            ctx.guild.audioManager.connectionListener = object : ConnectionListener {
                override fun onStatusChange(status: ConnectionStatus) {
                    if (status == ConnectionStatus.CONNECTED)
                        play(ctx, manager)
                }

                override fun onUserSpeaking(user: User, speaking: Boolean) {
                    return
                }

                override fun onPing(ping: Long) {
                    return
                }
            }
        } else
            play(ctx, MusicManager.musicManagers[ctx.guild.id]!!)
    }

    fun play(ctx: Context, manager: GuildMusicManager) {
        val search = ctx.rawArgs.joinToString(" ")

        // TODO change translations from "download" to "add"

        MusicManager.playerManager.loadItemOrdered(manager, search, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) = ctx.send("Failed to add song to queue: ${exception.message}")

            override fun noMatches() {
                val picker = ItemPicker(EventListener.instance.waiter, ctx.member as Member, ctx.guild as Guild, true)

                val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
                    url(HttpUrl.Builder().apply {
                        scheme("https")
                        host("www.googleapis.com")
                        addPathSegment("youtube")
                        addPathSegment("v3")
                        addPathSegment("search")
                        addQueryParameter("key", Akatsuki.instance.config.api.google)
                        addQueryParameter("part", "snippet")
                        addQueryParameter("maxResults", "10")
                        addQueryParameter("type", "video")
                        addQueryParameter("q", search)
                    }.build())
                }.build()).execute()

                val items = JSONObject(res.body()!!.string()).getJSONArray("items")

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)

                    val id = item
                            .getJSONObject("id")
                            .getString("videoId")

                    val snippet = item.getJSONObject("snippet")

                    val title = snippet.getString("title")
                    val thumb = snippet
                            .getJSONObject("thumbnails")
                            .getJSONObject("medium")
                            .getString("url")

                    val channel = snippet.getString("channelTitle")

                    picker.addItem(PickerItem(id, title, "", channel, thumb, url = "https://youtu.be/$id"))
                }

                picker.color = Color(255, 0, 0)

                val item = picker.build(ctx.channel).get()

                MusicManager.playerManager.loadItemOrdered(manager, item.url, object : AudioLoadResultHandler {
                    override fun loadFailed(exception: FriendlyException) = ctx.send("Failed to add song to queue: ${exception.message}")

                    override fun noMatches() = ctx.send("Woops! Couldn't find that!")

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
                val id = when {
                    search.indexOf("youtu") > -1 -> search.split("v=")[1].split("&")[0]

                    else -> ""
                }

                val tracks = if (id.isNotBlank()) {
                    val index = playlist.tracks.indexOfFirst { it.identifier == id }

                    playlist.tracks.subList(index, playlist.tracks.size)
                } else
                    playlist.tracks



                for (track in tracks) {
                    manager.scheduler.add(track)
                }

                ctx.send("Added ${tracks.size} tracks from playlist ${playlist.name} to the queue!")
            }
        })
    }
}