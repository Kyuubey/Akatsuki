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
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.Request
import org.json.JSONObject

@Load
class Info : Command() {
    override val desc = "Get info on the bot."

    override fun run(ctx: Context) {
        val embed = EmbedBuilder().apply {
            val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
                url("https://api.github.com/repos/noud02/Akatsuki")
            }.build()).execute()

            val json = JSONObject(res.body()!!.string())
            val stars = json.getInt("stargazers_count")
            val issues = json.getInt("open_issues")
            val forks = json.getInt("forks_count")

            setTitle(ctx.jda.selfUser.name)

            descriptionBuilder.append("This bot runs on [Akatsuki](https://github.com/noud02/Akatsuki), an open-source Discord bot written in Kotlin.\n")
            descriptionBuilder.append("\n${Akatsuki.instance.config.description}\n\n")
            descriptionBuilder.append("[Bugs](https://github.com/noud02/Akatsuki/issues) | [GitHub](https://github.com/noud02/Akatsuki)\n")
            descriptionBuilder.append("\n\u2B50 $stars | \u26A0 $issues | \uD83C\uDF74 $forks\n")
        }
        
        ctx.send(embed.build())
    }
}