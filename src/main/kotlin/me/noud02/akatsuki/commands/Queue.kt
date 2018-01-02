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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.entities.PickerItem
import me.noud02.akatsuki.music.MusicManager
import me.noud02.akatsuki.utils.ItemPicker
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.awt.Color

@Perm(Permission.MANAGE_SERVER)
class Clear : Command() {
    override val desc = "Clear the queue!"

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")

        manager.scheduler.queue.clear()
        ctx.send("Cleared the queue!")
    }
}

@Load
class Queue : Command() {
    override val desc = "View the queue!"
    override val guildOnly = true

    init {
        addSubcommand(Clear())
    }

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connected!")

        val formatted = manager.scheduler.queue.mapIndexed {
            i: Int, audioTrack: AudioTrack -> "${i + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})"
        }.joinToString("\n")

        if (formatted.length > 2048) {
            val parts = mutableListOf<String>()
            val picker = ItemPicker(ctx.client.waiter, ctx.member!!, ctx.guild)
            var part = ""

            val items = manager.scheduler.queue.mapIndexed {
                i: Int, audioTrack: AudioTrack -> "${i + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})"
            }

            for (item in items) {
                if (part.split("\n").size >= 10) {
                    parts += part
                    part = ""
                }

                part += "$item\n"
            }

            if (part.isNotBlank() && part.split("\n").size <= 10)
                parts += part

            for (pt in parts) {
                picker.addItem(
                        PickerItem(
                                "",
                                ctx.lang.getString("queue"),
                                pt
                        )
                )
            }

            picker.build(ctx.channel)
        } else {
            val embed = EmbedBuilder().apply {
                setColor(Color.CYAN)
                setTitle("${ctx.lang.getString("queue")}:")
                descriptionBuilder.append(formatted)
            }

            ctx.send(embed.build())
        }
    }
}