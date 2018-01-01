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
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.utils.ItemPicker
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import java.awt.Color

@Load
@Argument("query", "string")
@Alias("yt")
class Youtube : Command() {
    override val desc = "Search for videos on YouTube"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val picker = ItemPicker(ctx.client.waiter, ctx.member as Member, ctx.guild as Guild)

        val res = khttp.get(
                "https://www.googleapis.com/youtube/v3/search", 
                mapOf(),
                mapOf(
                        "key" to ctx.client.config.api.google, 
                        "part" to "snippet", 
                        "maxResults" to "10", 
                        "type" to "video",
                        "q" to ctx.args["query"] as String
                )
        )

        val items = res
                .jsonObject
                .getJSONArray("items")

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

            val desc = snippet.getString("description")
            val channel = snippet.getString("channelTitle")

            picker.addItem(PickerItem(id, title, desc, channel, thumb, url = "https://youtu.be/$id"))
        }

        picker.color = Color(255, 0, 0)

        picker.build(ctx.channel)
    }
}