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
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.entities.ThreadedCommand
import net.dv8tion.jda.core.entities.Member
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

@Load
@Argument("user", "user")
class ShitWaifu : ThreadedCommand() {
    override fun threadedRun(ctx: Context) {
        val member = ctx.args["user"] as Member

        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url("https://api.weeb.sh/auto-image/waifu-insult")
            header("Authorization", "Wolke ${Akatsuki.instance.config.api.weebsh}")
            post(RequestBody.create(MediaType.parse("application/json"), "{\"avatar\":\"${member.user.avatarUrl}\"}"))
        }.build()).execute()

        ctx.channel.sendFile(res.body()!!.bytes(), "shitwaifu.png").queue()
        res.close()
    }
}