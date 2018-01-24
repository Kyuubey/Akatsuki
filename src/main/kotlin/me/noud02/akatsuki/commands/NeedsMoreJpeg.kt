/*
 *   Copyright (c) 2017 Noud Kerver
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

import khttp.extensions.fileLike
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

@Load
@Argument("image", "url", true)
class NeedsMoreJpeg : Command() {
    override fun run(ctx: Context) {
        val temp = File.createTempFile("image", "png")
        temp.deleteOnExit()
        val out = FileOutputStream(temp)
        IOUtils.copy(
                ctx.msg.attachments.getOrNull(0)?.inputStream
                        ?: if (ctx.args["image"] as? String != null)
                            khttp.get(ctx.args["image"] as String).content.inputStream()
                        else
                            ctx.getLastImage()
                                    ?: return ctx.send("No images found!"),
                out
        )


        val req = khttp.post(
                "${
                if (Akatsuki.instance.config.backend.ssl) "https" else "http"
                }://${
                Akatsuki.instance.config.backend.host
                }${
                if (Akatsuki.instance.config.backend.port != 80) ":${Akatsuki.instance.config.backend.port}" else ""
                }/api/needsmorejpeg",
                files = listOf(temp.fileLike())
        )

        ctx.channel.sendFile(req.content, "needsmorejpeg.jpg", null).queue()
    }
}