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