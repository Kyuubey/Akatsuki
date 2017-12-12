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

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.schema.Guilds
import me.noud02.akatsuki.entities.AsyncCommand
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.extensions.addStar
import net.dv8tion.jda.core.entities.TextChannel
import org.jetbrains.exposed.sql.update

// import me.noud02.akatsuki.extensions.addStar

@Argument("channel", "textchannel")
class SetChannel : AsyncCommand() {
    override val guildOnly = true

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
            Guilds.update({
                Guilds.id.eq(ctx.guild!!.idLong)
            }) {
                it[starboardChannel] = (ctx.args["channel"] as TextChannel).idLong
            }
        }.await()
        ctx.send("Changed starboard channel to ${(ctx.args["channel"] as TextChannel).asMention}")
    }
}

@Load
class Starboard : AsyncCommand() {
    override val guildOnly = true

    init {
        addSubcommand(SetChannel())
    }

    override suspend fun asyncRun(ctx: Context) {
        if (!ctx.storedGuild!!.starboard)
            asyncTransaction(ctx.client.pool) {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    it[starboard] = true
                }
            }.await()
        ctx.guild!!.addStar(ctx.msg, ctx.author)
    }
}