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