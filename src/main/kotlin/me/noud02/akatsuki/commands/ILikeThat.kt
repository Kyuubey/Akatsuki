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

import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

// @Load
// TODO fix this command
@Argument("text", "string")
class ILikeThat : Command() {
    override val name = "ilikethat"
    override val desc = "It's OK, I like that..."

    override fun run(ctx: Context) {
        val img = ImageIO.read(File("./src/main/resources/img/ilikethat.png"))
        val g = img.createGraphics()
        val txtImg = BufferedImage(125, 60, BufferedImage.TRANSLUCENT)
        val txtG = txtImg.createGraphics()
        val out = ByteArrayOutputStream()



        txtG.font = Font("Noto Sans", Font.PLAIN, 40 - ctx.rawArgs.joinToString(" ").length * 2)
        txtG.color = Color.BLACK
        txtG.rotate(Math.toRadians(-19.0))
        txtG.drawString(ctx.args["text"] as String, 0, 0)
        txtG.dispose()

        g.drawImage(txtImg, 175, 160, 125, 60, null)
        g.dispose()

        ImageIO.write(img, "png", out)

        ctx.event.channel.sendFile(ByteArrayInputStream(out.toByteArray()), "ilikethat.png", null).queue()
    }
}