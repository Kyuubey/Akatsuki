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
import me.noud02.akatsuki.annotations.Flag
import me.noud02.akatsuki.annotations.Flags
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.entities.ThreadedCommand
import me.noud02.akatsuki.utils.I18n
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONArray
import java.awt.Color
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

@Load
@Argument("username", "string")
@Flags(
        Flag("taiko", 't', "View taiko stats"),
        Flag("mania", 'm', "View osu!mania stats"),
        Flag("catch", 'c', "View catch the beat stats")
)
class Osu : ThreadedCommand() {
    override fun threadedRun(ctx: Context) {
        val username = ctx.args["username"] as String
        val mode = when {
            ctx.flags.argMap.containsKey("taiko") || ctx.flags.argMap.containsKey("t") -> "1"
            ctx.flags.argMap.containsKey("catch") || ctx.flags.argMap.containsKey("c") -> "2"
            ctx.flags.argMap.containsKey("mania") || ctx.flags.argMap.containsKey("m") -> "3"
            else -> "0"
        }
        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("osu.ppy.sh")
                addPathSegment("api")
                addPathSegment("get_user")
                addQueryParameter("k", Akatsuki.instance.config.api.osu)
                addQueryParameter("u", username)
                addQueryParameter("m", mode)
                addQueryParameter("type", "string")
            }.build())
        }.build()).execute()

        val body = res.body()!!.string()
        val json = JSONArray(body)

        if (json.length() == 0)
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("user_not_found"),
                            mapOf("username" to ctx.author.name)
                    )
            )

        val user = json.getJSONObject(0)

        val bestRes = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme("https")
                host("osu.ppy.sh")
                addPathSegment("api")
                addPathSegment("get_user_best")
                addQueryParameter("k", Akatsuki.instance.config.api.osu)
                addQueryParameter("u", user.getString("user_id"))
                addQueryParameter("m", mode)
                addQueryParameter("type", "id")
            }.build())
        }.build()).execute()

        val bestBody = bestRes.body()!!.string()
        val bestJson = JSONArray(bestBody)

        val embed = EmbedBuilder().apply {
            setTitle("${user.getString("username")} (${user.getString("country")})", "https://osu.ppy.sh/users/${user.getString("user_id")}")
            setColor(Color(232, 102, 160))
            descriptionBuilder.append("**Level:** ${floor(user.getString("level").toFloat()).roundToInt()}\n")
            descriptionBuilder.append("**Plays:** ${user.getString("playcount")}\n")
            descriptionBuilder.append("**Accuracy:** ${round(user.getString("accuracy").toFloat() * 100) / 100}%\n")
            descriptionBuilder.append("**Score:** ${user.getString("ranked_score")}\n")
            descriptionBuilder.append("**Rank:** ${user.getString("pp_rank")}\n")
            descriptionBuilder.append("**PP:** ${user.getString("pp_raw").toFloat().roundToInt()}\n")
            descriptionBuilder.append("\n\uD83C\uDFC6 **Best Plays** \uD83C\uDFC6")

            for (i in 0 until min(bestJson.length(), 5)) {
                val best = bestJson.getJSONObject(i)

                val beatmapRes = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
                    url(HttpUrl.Builder().apply {
                        scheme("https")
                        host("osu.ppy.sh")
                        addPathSegment("api")
                        addPathSegment("get_beatmaps")
                        addQueryParameter("k", Akatsuki.instance.config.api.osu)
                        addQueryParameter("b", best.getString("beatmap_id"))
                    }.build())
                }.build()).execute()

                val beatmapBody = beatmapRes.body()!!.string()
                val beatmap = JSONArray(beatmapBody).getJSONObject(0)

                addField(
                        "${beatmap.getString("artist")} - ${beatmap.getString("title")} [${beatmap.getString("version")}] (${best.getString("rank").replace("X", "SS")})",
                        "**Score:** ${best.getString("score")}\n" +
                                "**Combo:** ${best.getString("maxcombo")}\n" +
                                "**PP:** ${best.getString("pp").toFloat().roundToInt()}",
                        true
                )

                beatmapRes.close()
            }
        }.build()

        ctx.channel.sendMessage(embed).queue {
            res.close()
            bestRes.close()
        }
    }
}