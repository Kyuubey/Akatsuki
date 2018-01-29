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
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.entities.ThreadedCommand
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONObject

@Load
@Argument("term", "string")
class Urban : ThreadedCommand() {
    override val desc = "Search on the urban dictionary!"

    override fun threadedRun(ctx: Context) {
        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("api.urbandictionary.com")
                addPathSegment("v0")
                addPathSegment("define")
                addQueryParameter("term", ctx.args["term"] as String)
            }.build())
        }.build()).execute()

        val json = JSONObject(res.body()!!.string())

        if (json.getString("result_type") == "no_results")
            return ctx.send("No results found!")

        val item = json.getJSONArray("list").getJSONObject(0)

        val embed = EmbedBuilder().apply {
            setAuthor(item.getString("author"))
            setTitle(item.getString("word"), item.getString("permalink"))
            descriptionBuilder.append(item.getString("definition"))
            descriptionBuilder.append("\n\n${item.getString("example")}")
            setFooter("${item.getInt("thumbs_up")} \uD83D\uDC4D | ${item.getInt("thumbs_down")} \uD83D\uDC4E", null)
        }

        ctx.send(embed.build())
    }
}