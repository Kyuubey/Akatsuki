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
import me.noud02.akatsuki.annotations.Arguments
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.entities.Member
import okhttp3.*
import java.io.File
import java.io.FileOutputStream

@Load
@Arguments(
        Argument("user1", "user"),
        Argument("user2", "user")
)
class Ship : Command() {
    override val desc = "Ship people with eachother"

    override fun run(ctx: Context) {
        val user1 = ctx.args["user1"] as Member
        val user2 = ctx.args["user2"] as Member

        val temp1 = File.createTempFile("image", "png")
        temp1.deleteOnExit()

        val temp2 = File.createTempFile("image", "png")
        temp2.deleteOnExit()

        temp1.writeBytes(
                Akatsuki.instance.okhttp
                        .newCall(Request.Builder().url(user1.user.avatarUrl).build())
                        .execute()
                        .body()!!
                        .bytes()
        )
        temp2.writeBytes(
                Akatsuki.instance.okhttp
                        .newCall(Request.Builder().url(user2.user.avatarUrl).build())
                        .execute()
                        .body()!!
                        .bytes()
        )

        val first = user1.user.name
        val sec = user2.user.name

        val ship = first.substring(0, Math.floor(first.length / 2.0).toInt()) + sec.substring(Math.floor(sec.length / 2.0).toInt())

        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme(if (Akatsuki.instance.config.backend.ssl) "https" else "http")
                host(Akatsuki.instance.config.backend.host)
                port(Akatsuki.instance.config.backend.port)
                addPathSegment("api")
                addPathSegment("ship")
            }.build())
            post(MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                addFormDataPart(
                        "user1",
                        "avatar.png",
                        RequestBody.create(MediaType.parse("image/png"), temp1)
                )
                addFormDataPart(
                        "user2",
                        "avatar2.png",
                        RequestBody.create(MediaType.parse("image/png"), temp2)
                )
            }.build())
        }.build()).execute()

        ctx.channel
                .sendMessage("Happy shipping!\nYour shipname: $ship")
                .addFile(res.body()!!.byteStream(), "ship.png")
                .queue()
    }
}