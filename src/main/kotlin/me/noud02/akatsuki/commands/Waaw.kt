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

import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import okhttp3.*
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

@Load
@Argument("image", "url", true)
class Waaw : Command() {
    override fun run(ctx: Context) {
        val temp = File.createTempFile("image", "png")
        temp.deleteOnExit()
        val out = FileOutputStream(temp)
        IOUtils.copy(
                ctx.msg.attachments.getOrNull(0)?.inputStream
                        ?: if (ctx.args.containsKey("image"))
                            Akatsuki.instance.okhttp
                                    .newCall(Request.Builder().url(ctx.args["image"] as String).build())
                                    .execute()
                                    .body()!!
                                    .byteStream()
                        else
                            ctx.getLastImage() ?: return ctx.send("No images found!"),
                out
        )


        val res = Akatsuki.instance.okhttp.newCall(Request.Builder().apply {
            url(HttpUrl.Builder().apply {
                scheme(if (Akatsuki.instance.config.backend.ssl) "https" else "http")
                host(Akatsuki.instance.config.backend.host)
                port(Akatsuki.instance.config.backend.port)
                addPathSegment("api")
                addPathSegment("waaw")
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

        ctx.channel.sendFile(res.body()!!.byteStream(), "waaw.png", null).queue()
    }
}