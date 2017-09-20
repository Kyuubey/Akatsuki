package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.objdetect.CascadeClassifier
import java.awt.image.DataBufferByte
import java.net.URL
import javax.imageio.ImageIO

// @Load
// TODO need to build opencv and fix this
@Argument("img", "url")
class Eyes : Command() {
    override val name = "eyes"
    override val desc = ":eyes:"

    init {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
    }

    override fun run(ctx: Context) {
        val img = ImageIO.read(URL("https://nyc3.digitaloceanspaces.com/awoo/abe42535fc1b8e9165c01ee21c283194.png?AWSAccessKeyId=SVXI56BDCZ5EQPUGGZ6I&Expires=1505914385&Signature=ELWlEY5ehOl65FnR6PBVOVbmFqM%3D"))
        println("1")
        val detector = CascadeClassifier("../../../../../resources/cascade/haarcascade_eye.xml")
        println("2")
        val animeDetector = CascadeClassifier("../../../../../resources/cascade/lbpcascade_animeface.xml")
        println("3")
        val mat = Mat()
        val pixels = (img.raster.dataBuffer as DataBufferByte).data
        val faces = MatOfRect()

        val res = mutableListOf<String>()

        mat.put(0, 0, pixels)

        detector.detectMultiScale(mat, faces)

        if (faces.toArray().isEmpty())
            animeDetector.detectMultiScale(mat, faces)

        faces.toArray().mapTo(res) { "Found face at x=${it.x} y=${it.y}" }

        if (res.isNotEmpty())
            ctx.send(res.joinToString("\n"))
        else
            ctx.send("No faces found!")
    }
}