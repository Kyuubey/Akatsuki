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
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.music.MusicManager
import me.noud02.akatsuki.utils.I18n
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
@Argument("volume", "number", true)
class Volume : Command() {
    override val desc = "Change the volume of the music."
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connected!")

        if (ctx.args["volume"] != null) {
            val vol = ctx.args["volume"] as Int

            if (vol > 100)
                return ctx.send(ctx.lang.getString("too_loud"))

            if (vol < 0)
                return ctx.send(ctx.lang.getString("cant_hear"))

            manager.player.volume = vol
        }

        ctx.send(
                I18n.parse(
                        ctx.lang.getString("volume_changed"),
                        mapOf(
                                "volume" to "#".repeat(manager.player.volume / 10) + "-".repeat(10 - manager.player.volume / 10),
                                "number" to manager.player.volume
                        )
                )
        )
    }
}