@file:Suppress("DEPRECATION")

package me.noud02.akatsuki.commands

import khttp.structures.authorization.BasicAuthorization
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONArray
import org.json.JSONObject
import org.json.XML

@Load
@Argument("anime", "string")
class Anime : Command() {
    override val desc = "Search for anime on MyAnimeList"

    override fun run(ctx: Context) {
        val req = khttp.get(
                "https://myanimelist.net/api/anime/search.xml",
                params = mapOf("q" to ctx.args["anime"] as String),
                auth = BasicAuthorization(
                        Akatsuki.instance.config.api.myanimelist.split(":")[0],
                        Akatsuki.instance.config.api.myanimelist.split(":")[1]
                )
        )

        val json = XML
                .toJSONObject(req.text)
                .getJSONObject("anime")

        val entry = json.getJSONObject("entry") ?: json.getJSONArray("entry").getJSONObject(0)

        val embed = EmbedBuilder().apply {
            setTitle(entry.getString("title"))

            descriptionBuilder.append("${entry.getInt("score")} \u2606 | ${entry.getDouble("episodes")} ${
            when (entry.getString("type")) {
                "TV" -> "\uD83D\uDCFA"

                "MOVIE" -> "\uD83C\uDF7F"
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
            setImage(entry.getString("image"))
        }

        ctx.send(embed.build())
    }
}