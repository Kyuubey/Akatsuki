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

import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.entities.ThreadedCommand
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color
import kotlin.math.min

@Load
@Argument("query", "string")
@Alias("ddg")
class DuckDuckGo : ThreadedCommand() {
    override fun threadedRun(ctx: Context) {
        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("api.duckduckgo.com")
                addQueryParameter("format", "json")
                addQueryParameter("q", ctx.args["query"] as String)
            }.build())
        }.build()).execute()

        val body = res.body()!!.string()
        val json = JSONObject(body)

        val embed = EmbedBuilder().apply {
            setColor(Color(222, 88, 51))

            if (json.optJSONObject("Infobox") != null) {
                val info = json.getJSONObject("Infobox")
                val content = info.getJSONArray("content")
                (0 until content.length())
                        .asSequence()
                        .map { content.getJSONObject(it) }
                        .filter { it.getString("data_type") == "string" }
                        .forEach { descriptionBuilder.append("**${it.getString("label")}:** ${it.getString("value")}\n") }
            }

            if (!json.optString("Entity").isNullOrBlank())
                setAuthor(json.getString("Entity"))

            if (!json.optString("Heading").isNullOrBlank())
                setTitle(json.getString("Heading"), json.optString("AbstractURL"))

            if (!json.optString("AbstractText").isNullOrBlank())
                descriptionBuilder.append("\n${json.getString("AbstractText")}\n")
            else if (!json.optString("Abstract").isNullOrBlank())
                descriptionBuilder.append("\n${json.getString("Abstract")}\n")

            if (!json.getString("Image").isNullOrBlank())
                setThumbnail(json.getString("Image"))

            if (json.optJSONArray("RelatedTopics") != null) {
                val topics = json.getJSONArray("RelatedTopics")

                if (topics.length() != 0)
                    descriptionBuilder.append("\n**Related Topics:**")

                (0 until min(3, topics.length()))
                        .asSequence()
                        .map { topics.getJSONObject(it) }
                        .forEach {
                            addField(
                                    if (it.getString("Text").length > 256) it.getString("Text").substring(0, 253) + "..." else it.getString("Text"),
                                    it.getString("FirstURL"),
                                    true
                            )
                        }
            }
        }

        if (embed.isEmpty)
            return ctx.channel.sendMessage(ctx.lang.getString("no_results")).queue {
                res.close()
            }

        ctx.channel.sendMessage(embed.build()).queue {
            res.close()
        }
    }
}