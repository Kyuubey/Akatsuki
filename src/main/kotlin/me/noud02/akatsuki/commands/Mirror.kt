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

import io.sentry.Sentry
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.entities.ImageCommand
import me.noud02.akatsuki.utils.Http
import okhttp3.*
import java.io.File

@Load
@Argument("image", "url", true)
class Mirror : ImageCommand() {
    override val desc = "Mirror images"

    override fun imageRun(ctx: Context, file: File) {
        val body = MultipartBody.Builder().apply {
            setType(MultipartBody.FORM)
            addFormDataPart(
                    "image",
                    "image",
                    RequestBody.create(MediaType.parse("image/${file.extension}"), file)
            )
        }.build()

        Http.post(HttpUrl.Builder().apply {
            scheme(if (Akatsuki.config.backend.ssl) "https" else "http")
            host(Akatsuki.config.backend.host)
            port(Akatsuki.config.backend.port)
            addPathSegment("api")
            addPathSegment("mirror")
        }.build(), body).thenAccept { res ->
            val bytes = res.body()!!.byteStream()
            ctx.channel.sendFile(bytes, "mirror.${file.extension}", null).queue()
            res.close()
            file.delete()
        }.thenApply {}.exceptionally {
            ctx.logger.error("Error while trying to generate mirrored image", it)
            ctx.sendError(it)
            Sentry.capture(it)
        }
    }
}