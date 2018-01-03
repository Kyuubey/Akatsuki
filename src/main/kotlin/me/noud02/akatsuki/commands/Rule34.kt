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

import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.utils.I18n
import net.dv8tion.jda.core.EmbedBuilder
import org.json.XML

@Load
@Argument("tags", "string")
class Rule34 : Command() {
    override val nsfw = true
    override val desc = "Search for (nsfw) images on rule34"

    override fun run(ctx: Context) {
        val tags = ctx.args["tags"] as String

        if (tags.indexOf("loli") > -1)
            return ctx.send(I18n.parse(ctx.lang.getString("loli_is_illegal"), mapOf("username" to ctx.author.name)))

        val req = khttp.get(
                "https://rule34.xxx/index.php",
                params = mapOf(
                        "page" to "dapi",
                        "s" to "post",
                        "q" to "index",
                        "tags" to tags
                )
        )

        val posts = XML
                .toJSONObject(req.text)
                .getJSONObject("posts")
                .getJSONArray("post")

        val post = posts.getJSONObject(Math.floor(Math.random() * posts.length()).toInt())

        if (post.getString("tags").indexOf("loli") > -1)
            return ctx.send(I18n.parse(ctx.lang.getString("loli_is_illegal"), mapOf("username" to ctx.author.name)))

        val embed = EmbedBuilder().apply {
            setImage(post.getString("file_url"))
        }

        ctx.send(embed.build())
    }
}