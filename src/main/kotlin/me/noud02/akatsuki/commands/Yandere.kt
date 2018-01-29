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
import me.noud02.akatsuki.utils.I18n
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONArray

@Load
@Argument("tags", "string")
class Yandere : Command() {
    override val nsfw = true
    override val desc = "Search for (lewd) images on yande.re"

    override fun run(ctx: Context) {
        val query = ctx.args["tags"] as String

        if (query.indexOf("loli") > -1)
            return ctx.send(I18n.parse(ctx.lang.getString("loli_is_illegal"), mapOf("username" to ctx.author.name)))

        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("yande.re")
                addPathSegment("post.json")
                addQueryParameter("limit", "100")
                addQueryParameter("tags", query)
            }.build())
        }.build()).execute()

        val jsonArr = JSONArray(res.body()!!.string())

        if (jsonArr.count() == 0)
            return ctx.send(I18n.parse(ctx.lang.getString("no_images_found"), mapOf("username" to ctx.author.name)))

        val json = jsonArr.getJSONObject(Math.floor(Math.random() * jsonArr.count()).toInt())

        if (json.getString("tags").indexOf("loli") > -1)
            return ctx.send(I18n.parse(ctx.lang.getString("loli_is_illegal"), mapOf("username" to ctx.author.name)))

        val embed = EmbedBuilder().apply {
            setImage(json.getString("file_url"))
        }

        ctx.send(embed.build())
    }
}