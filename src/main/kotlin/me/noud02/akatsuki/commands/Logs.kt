package me.noud02.akatsuki.commands

import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Flag
import me.noud02.akatsuki.annotations.Flags
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.http.client.utils.URIBuilder
import java.awt.Color

@Load
@Argument("logs", "number", true)
@Flags(
        Flag("event", 'e', "Event type, can be DELETE, UPDATE or CREATE."),
        Flag("delete", 'd', "Sets event type to DELETE."),
        Flag("create", 'c', "Sets event type to CREATE."),
        Flag("update", 'u', "Sets event type to UPDATE.")
)
class Logs : Command() {
    override fun run(ctx: Context) {
        val num = ctx.args.getOrDefault("logs", 100)
        val url = URIBuilder().apply {
            scheme = if (ctx.client.config.site.ssl) "https" else "http"
            host = ctx.client.config.site.host
            if (ctx.client.config.site.port != 80)
                port = ctx.client.config.site.port
            path = "/logs/${ctx.channel.id}/${ctx.msg.creationTime.toInstant().toEpochMilli()}"

            when {
                ctx.flags.argMap.contains("event") || ctx.flags.argMap.contains("e") ->
                    addParameter(
                            "event",
                            ctx.flags.argMap.getOrDefault(
                                    "event",
                                    ctx.flags.argMap.getOrDefault(
                                            "e",
                                            "ALL"
                                    )
                            )
                    )

                ctx.flags.argMap.contains("delete") || ctx.flags.argMap.contains("d") ->
                    addParameter("event", "DELETE")

                ctx.flags.argMap.contains("create") || ctx.flags.argMap.contains("c") ->
                    addParameter("event", "CREATE")

                ctx.flags.argMap.contains("update") || ctx.flags.argMap.contains("u") ->
                    addParameter("event", "UPDATE")
            }

            addParameter("limit", num.toString())
        }
        val embed = EmbedBuilder().apply {
            setTitle("Logs for #${ctx.channel.name} ($num)", url.build().toString())
            setColor(Color.CYAN)
        }

        ctx.send(embed.build())
    }
}