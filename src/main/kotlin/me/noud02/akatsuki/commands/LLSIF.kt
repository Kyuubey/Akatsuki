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
import me.noud02.akatsuki.entities.AsyncCommand
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

// @Load
@Argument("card", "string")
class GetCard : Command() {
    override val name = "card"
    override val desc = "Get info on a card"

    override fun run(ctx: Context) {
        val card = ctx.args["card"] as String
        val apiBase = "https://schoolido.lu/api"
        val attrColors = mapOf(
                "Cool" to Color(0x11C2FF),
                "Smile" to Color(0xEE1A8D),
                "Pure" to Color(0x00BB42),
                "All" to Color(0xd8bff8)
        )

        if (card.toIntOrNull() != null) {
            val res = khttp.get("$apiBase/cards/$card?expand_idol")

            if (res.statusCode != 200)
                return ctx.sendError(Throwable("Expected status code 200, got ${res.statusCode}"))

            val json = res.jsonObject

            try {
                json.getString("detail")
                return ctx.send("Not Found!")
            } catch (e: Throwable) {
            }

            val idol = json.getJSONObject("idol")

            val embed = EmbedBuilder().apply {
                setTitle(
                        "${
                        idol.getString("name")
                        } (${
                        idol.getString("japanese_name")
                        }) [${
                        json.getString("rarity")
                        }]${
                        if (json.getBoolean("japan_only")) " \u1F1EF\u1F1F5" else ""
                        }",
                        idol.getString("website_url")
                )
                setColor(attrColors[json.getString("attribute")])
            }

            ctx.send(embed.build())
        }
    }
}

// @Load
class LLSIF : AsyncCommand() {
    override val desc = "Get info on cards and other things related to Love Live! School Idol Festival."

    init {
        addSubcommand(GetCard())
    }
}