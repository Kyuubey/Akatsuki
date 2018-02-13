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
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.entities.ThreadedCommand
import me.noud02.akatsuki.utils.I18n
import okhttp3.*
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

@Load
@Argument("image", "url", true)
class Haah : ThreadedCommand() {
    override fun threadedRun(ctx: Context) {
        val temp = File.createTempFile("image", "png")
        temp.deleteOnExit()
        val out = FileOutputStream(temp)

        when {
            ctx.msg.attachments.isNotEmpty() -> {
                IOUtils.copy(ctx.msg.attachments[0].inputStream, out)
                command(ctx, temp)
            }

            ctx.args.containsKey("image") -> {
                val imgRes = Akatsuki.instance.okhttp
                        .newCall(Request.Builder().url(ctx.args["image"] as String).build())
                        .execute()

                val bytes = imgRes.body()!!.bytes()
                temp.writeBytes(bytes)
                command(ctx, temp)
                imgRes.close()
            }


            else -> ctx.getLastImage()
                    .thenAccept {
                        if (it == null)
                            return@thenAccept ctx.send(
                                    I18n.parse(
                                            ctx.lang.getString("no_images_channel"),
                                            mapOf("username" to ctx.author.name)
                                    )
                            )

                        IOUtils.copy(it, out)
                        command(ctx, temp)
                    }
        }
    }

    fun command(ctx: Context, temp: File) {
        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme(if (Akatsuki.instance.config.backend.ssl) "https" else "http")
                host(Akatsuki.instance.config.backend.host)
                port(Akatsuki.instance.config.backend.port)
                addPathSegment("api")
                addPathSegment("haah")
            }.build())
            post(MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                addFormDataPart(
                        "image",
                        "image",
                        RequestBody.create(MediaType.parse("image/${temp.extension}"), temp)
                )
            }.build())
        }.build()).execute()

        ctx.channel.sendFile(res.body()!!.byteStream(), "haah.png", null).queue {
            res.close()
        }
    }
}