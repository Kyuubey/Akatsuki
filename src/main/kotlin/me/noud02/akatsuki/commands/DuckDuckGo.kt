/*
 *   Copyright (c) 2017 Noud Kerver
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
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONObject

@Load
@Argument("query", "string")
class DuckDuckGo : Command() {
    override fun run(ctx: Context) {
        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("api.duckduckgo.com")
                addQueryParameter("format", "json")
                addQueryParameter("q", ctx.args["query"] as String)
            }.build())
        }.build()).execute()

        val json = JSONObject(res.body()!!.string())

        val embed = EmbedBuilder().apply {
            if (!json.getString("Heading").isNullOrBlank())
                setTitle("${
                json.getString("Heading")
                }${
                if (!json.getString("Entity").isNullOrBlank())
                    " (${json.getString("Entity")})"
                else
                    ""
                }")

            if (!json.getString("AbstractText").isNullOrBlank())
                descriptionBuilder.append(json.getString("AbstractText"))

            if (!json.getString("Image").isNullOrBlank())
                setThumbnail(json.getString("Image"))
        }

        ctx.send(embed.build())
    }
}