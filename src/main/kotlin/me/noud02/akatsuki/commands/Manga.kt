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

@file:Suppress("DEPRECATION")

package me.noud02.akatsuki.commands

import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Request
import org.apache.commons.lang3.StringEscapeUtils
import org.json.XML
import java.awt.Color

@Load
@Argument("manga", "string")
class Manga : Command() {
    override val desc = "Search for manga on MyAnimeList"

    override fun run(ctx: Context) {
        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("myanimelist.net")
                addPathSegment("api")
                addPathSegment("manga")
                addPathSegment("search.xml")
                addQueryParameter("q", ctx.args["manga"] as String)
            }.build())

            val mal = Akatsuki.instance.config.api.myanimelist
            val cred = Credentials.basic(mal.split(":")[0], mal.split(":")[1])

            header("Authorization", cred)

        }.build()).execute()

        val json = XML
                .toJSONObject(res.body()!!.string())
                .getJSONObject("manga")

        val entry = json.getJSONObject("entry") ?: json.getJSONArray("entry").getJSONObject(0)

        val embed = EmbedBuilder().apply {
            setTitle(entry.getString("title"))

            descriptionBuilder.append("${
            entry.getDouble("score")
            } \u2606 | ${
            entry.getInt("volumes")
            } \uD83D\uDCDA ${
            entry.getInt("chapters")
            } ${
            when (entry.getString("type").toLowerCase()) {
                "manga" -> "\uD83D\uDCD6"
                else -> "?"
            }
            } ${entry.getString("status")} | ${
            if (entry.getString("start_date") != "0000-00-00")
                entry.getString("start_date")
            else
                "unknown"
            } -> ${
            if (entry.getString("end_date") != "0000-00-00")
                entry.getString("end_date")
            else
                "unknown"
            }")

            addField(
                    "Synopsis",
                    StringEscapeUtils
                            .unescapeHtml4(entry.getString("synopsis"))
                            .replace("<br />", "\n"),
                    false
            )
            setColor(Color.CYAN)
            setImage(entry.getString("image"))
        }

        ctx.send(embed.build())
    }
}