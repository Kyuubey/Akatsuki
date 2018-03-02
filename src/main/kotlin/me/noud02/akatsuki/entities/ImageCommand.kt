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

package me.noud02.akatsuki.entities

import me.noud02.akatsuki.utils.Http
import me.noud02.akatsuki.utils.I18n
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

abstract class ImageCommand : Command() {
    abstract fun imageRun(ctx: Context, file: File)

    override fun run(ctx: Context) {
        val temp = File.createTempFile("image", "png")
        temp.deleteOnExit()
        val out = FileOutputStream(temp)

        when {
            ctx.msg.attachments.isNotEmpty() -> {
                IOUtils.copy(ctx.msg.attachments[0].inputStream, out)
                imageRun(ctx, temp)
            }

            ctx.args.containsKey("image") -> Http.get(ctx.args["image"] as String).thenAccept { res ->
                val bytes = res.body()!!.bytes()
                temp.writeBytes(bytes)
                imageRun(ctx, temp)
                res.close()
            }

            else -> ctx.getLastImage().thenAccept { img ->
                if (img == null) {
                    return@thenAccept ctx.send(
                            I18n.parse(
                                    ctx.lang.getString("no_images_channel"),
                                    mapOf("username" to ctx.author.name)
                            )
                    )
                }

                IOUtils.copy(img, out)
                imageRun(ctx, temp)
            }
        }
    }
}