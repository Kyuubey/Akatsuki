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

import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.music.MusicManager
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

@Load
@Alias("np")
class NowPlaying : Command() {
    override val desc = "Get the current song"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!") // TODO add translations for "not connected"
        val embed = EmbedBuilder().apply {
            setAuthor(ctx.lang.getString("now_playing"), null, null)
            setTitle(manager.player.playingTrack.info.title)
            setColor(Color.CYAN)
        }

        if (manager.scheduler.queue.isNotEmpty())
            embed.setFooter("Next: ${manager.scheduler.queue.peek().info.title}", null) // TODO add translations for "next"
        else if (manager.autoplay && manager.player.playingTrack.info.uri.indexOf("youtube") > -1) {
            val res = khttp.get(
                    "https://www.googleapis.com/youtube/v3/search",
                    mapOf(),
                    mapOf(
                            "key" to ctx.client.config.api.google,
                            "part" to "snippet",
                            "maxResults" to "10",
                            "type" to "video",
                            "relatedToVideoId" to manager.player.playingTrack.info.identifier
                    )
            )

            val title = res.jsonObject
                    .getJSONArray("items")
                    .getJSONObject(0)
                    .getJSONObject("snippet")
                    .getString("title")

            embed.setFooter("Next: $title (autoplay)", null)
        }

        ctx.send(embed.build())
    }
}