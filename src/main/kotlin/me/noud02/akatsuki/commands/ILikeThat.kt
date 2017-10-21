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

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import java.awt.Color
import java.awt.Font
import java.io.*
import java.net.URL
import javax.imageio.ImageIO

// @Load
// TODO fix this command
@Argument("text", "string")
class ILikeThat : Command() {
    override val name = "ilikethat"
    override val desc = "It's OK, I like that..."

    override fun run(ctx: Context) {
        val img = ImageIO.read(URL("https://kyubey.info/images/ilikethat.png"))
        val g = img.createGraphics()
        val out = ByteArrayOutputStream()

        // TODO wrap text instead of making font smaller
        g.font = Font("Comic Sans MS", Font.PLAIN, 40 - ctx.rawArgs.joinToString(" ").length * 2)
        g.color = Color.BLACK
        g.rotate(Math.toRadians(-19.0))
        g.drawString(ctx.rawArgs.joinToString(" "), 125, 300)
        g.dispose()

        ImageIO.write(img, "png", out)

        ctx.event.channel.sendFile(ByteArrayInputStream(out.toByteArray()), "ilikethat.png", null).queue()
    }
}