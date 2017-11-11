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

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import net.dv8tion.jda.core.EmbedBuilder

@Load
class Info : Command() {
    override val name = "info"
    override val desc = "Get info on the bot."

    override fun run(ctx: Context) {
        val embed = EmbedBuilder()
        val desc = embed.descriptionBuilder
        val json = khttp.get("https://api.github.com/repos/noud02/Akatsuki").jsonObject
        val stars = json.getInt("stargazers_count")
        val issues = json.getInt("open_issues")
        val forks = json.getInt("forks_count")
        
        embed.setTitle(ctx.client.jda.selfUser.name)
        
        desc.append("This bot runs on [Akatsuki](https://github.com/noud02/Akatsuki), an open-source Discord bot.\n")
        desc.append("\n${ctx.client.config.description}\n\n")
        desc.append("[Bugs](https://github.com/noud02/Akatsuki/issues) | [GitHub](https://github.com/noud02/Akatsuki) | [Website](https://akatsuki.noud02.me)\n")
        desc.append("\n\u2B50 $stars | \u26A0 $issues | \uD83C\uDF74 $forks\n")
        
        embed.setDescription(desc)
        
        ctx.send(embed.build())
    }
}