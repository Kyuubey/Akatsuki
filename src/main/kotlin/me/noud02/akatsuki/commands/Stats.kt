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
            if (ctx.client.jda.shardInfo != null) {
                setTitle("Shard ${ctx.client.jda.shardInfo.shardString}")
            }

            val uptimeHours = TimeUnit.MILLISECONDS.toHours(rb.uptime)
            val uptimeMins = TimeUnit.MILLISECONDS.toMinutes(rb.uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(rb.uptime))
            val uptimeSecs = TimeUnit.MILLISECONDS.toSeconds(rb.uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(rb.uptime)) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toHours(rb.uptime))

            descriptionBuilder.append("**Uptime:** ${"%02d:%02d:%02d".format(uptimeHours, uptimeMins, uptimeSecs)}\n")
            descriptionBuilder.append("**Guilds:** ${ctx.client.jda.guilds.size}\n")
            descriptionBuilder.append("**Users:** ${ctx.client.jda.users.size}\n")
            descriptionBuilder.append("**Voice connections:** ${MusicManager.musicManagers.size}\n")
            descriptionBuilder.append("**Ping:** ${ctx.client.jda.ping}ms\n")
            descriptionBuilder.append("**Memory Usage:** ${rtime.totalMemory() / (1024 * 1024)}MB")
        }

        ctx.send(embed.build())
    }
}