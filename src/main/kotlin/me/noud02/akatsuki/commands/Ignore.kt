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

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.schema.Guilds
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.entities.TextChannel
import org.jetbrains.exposed.sql.update

@Load
@Argument("channel", "textchannel", true)
class Ignore : Command() {
    override val guildOnly = true
    override val desc = "Have the bot ignore channels"

    override fun run(ctx: Context) {
        asyncTransaction(Akatsuki.instance.pool) {
            if (ctx.args.containsKey("channel")) {
                val channel = ctx.args["channel"] as TextChannel

                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    it[ignoredChannels] = ctx.storedGuild!!.ignoredChannels.plus(channel.idLong).toTypedArray()
                }

                ctx.send("Now ignoring ${channel.asMention}.")
            } else {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    it[ignoredChannels] = ctx.storedGuild!!.ignoredChannels.plus(ctx.channel.idLong).toTypedArray()
                }

                ctx.send("Now ignoring this channel.")
            }
        }.execute()
    }
}