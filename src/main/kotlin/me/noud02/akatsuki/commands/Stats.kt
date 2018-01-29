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

import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.music.MusicManager
import net.dv8tion.jda.core.EmbedBuilder
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

@Load
class Stats : Command() {
    override val desc = "Get stats of the bot."

    override fun run(ctx: Context) {
        val rb = ManagementFactory.getRuntimeMXBean()
        val rtime = Runtime.getRuntime()

        val embed = EmbedBuilder().apply {
            if (ctx.jda.shardInfo != null) {
                setTitle("Shard [${ctx.jda.shardInfo.shardId + 1} / ${ctx.jda.shardInfo.shardTotal}]")
            }

            val uptimeHours = TimeUnit.MILLISECONDS.toHours(rb.uptime)
            val uptimeMins = TimeUnit.MILLISECONDS.toMinutes(rb.uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(rb.uptime))
            val uptimeSecs = TimeUnit.MILLISECONDS.toSeconds(rb.uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(rb.uptime)) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toHours(rb.uptime))

            descriptionBuilder.append("**Uptime:** ${"%02d:%02d:%02d".format(uptimeHours, uptimeMins, uptimeSecs)}\n")
            descriptionBuilder.append("**Guilds:** ${ctx.jda.guilds.size}\n")
            descriptionBuilder.append("**Users:** ${ctx.jda.users.size}\n")
            descriptionBuilder.append("**Voice connections:** ${MusicManager.musicManagers.size}\n")
            descriptionBuilder.append("**Ping:** ${ctx.jda.ping}ms\n")
            descriptionBuilder.append("**Memory Usage:** ${rtime.totalMemory() / (1024 * 1024)}MB")
        }

        ctx.send(embed.build())
    }
}