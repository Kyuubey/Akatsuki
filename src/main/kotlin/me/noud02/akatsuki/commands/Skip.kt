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

import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.utils.I18n
import me.noud02.akatsuki.music.MusicManager
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member

@Load
@Perm(Permission.MANAGE_SERVER, true)
class Skip : Command() {
    override val desc = "Skips the current song"
    override val guildOnly = true

    // TODO add thing so you cant skip while there's nothing in the queue

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel())
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("join_voice_channel_fail"),
                            mapOf(
                                    "username" to ctx.author.name // TODO rename translation
                            )
                    )
            )
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connected!")

        if (manager.scheduler.queue.isEmpty())
            return ctx.send("There's nothing in the queue!")

        if (ctx.perms["MANAGE_SERVER"] == true) {
            manager.scheduler.next()
            ctx.send(
                    I18n.parse(
                            ctx.lang.getString("force_skip"),
                            mapOf(
                                    "username" to ctx.author.name
                            )
                    )
            )
        } else {
            val members = manager.voiceChannel.members.filter { !it.user.isBot }

            if (members.size - 1 <= manager.voteSkip.size) {
                manager.scheduler.next()

                return ctx.send(ctx.lang.getString("voteskip_success"))
            }

            if (manager.voteSkip.contains(ctx.author.id))
                return ctx.send(ctx.lang.getString("already_voted"))

            if (members.size - 1 <= manager.voteSkip.size + 1) {
                manager.scheduler.next()

                return ctx.send(ctx.lang.getString("voteskip_success"))
            }

            manager.voteSkip.add(ctx.author.id)
            ctx.send(
                    I18n.parse(
                            ctx.lang.getString("voteskip_add_success"),
                            mapOf(
                                    "votes" to manager.voteSkip.size,
                                    "total_votes" to members.size - 1
                            )
                    )
            )
        }
    }
}