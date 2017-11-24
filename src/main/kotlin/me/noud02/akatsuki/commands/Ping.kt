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

import khttp.get
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Flag
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.extensions.await
import net.dv8tion.jda.core.entities.Member
import java.time.temporal.ChronoUnit

@Flag("meme", 'm', "this is a meme")
class Pong : Command() {
    override val desc = "Ping!"

    override fun run(ctx: Context) {
        if (ctx.flags.argMap.containsKey("meme")) {
            val res = get("https://www.reddit.com/r/dankmemes.json")
                    .jsonObject
                    .getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(Math.floor(Math.random() * 10).toInt())
                    .getJSONObject("data")
                    .getString("url")
            ctx.send(res)
        } else
            ctx.send("Ping!")
    }
}

@Load
@Argument("meme", "user", true)
class Ping : AsyncCommand() {
    override val desc = "Pings a user if specified"

    init {
        addSubcommand(Pong())
    }

    override suspend fun asyncRun(ctx: Context) {
        if (ctx.args["meme"] != null && ctx.args["meme"] is Member) {
            val user = ctx.args["meme"] as Member
            ctx.send(user.asMention)
        } else {
            val msg = ctx.event.channel.sendMessage("Pong!").await()
            msg.editMessage("Pong! `${ctx.msg.creationTime.until(msg.creationTime, ChronoUnit.MILLIS)}ms`").queue()
        }
    }
}