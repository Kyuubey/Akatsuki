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
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.EventListener
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.DatabaseWrapper
import me.noud02.akatsuki.db.schema.Users
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import org.jetbrains.exposed.sql.update

@Load
@Argument("user", "user")
class Marry : Command() {
    override val guildOnly = true
    override val desc = "Marry people"

    override fun run(ctx: Context) {
        val member = ctx.args["user"] as Member

        if (ctx.storedUser.marriedUserId != 0L)
            return ctx.send("You are already married to ${ctx.jda.getUserById(ctx.storedUser.marriedUserId).name}!")

        if (member.user.id == ctx.author.id)
            return ctx.send("You can't marry yourself!")

        if (member.user.isBot)
            return ctx.send("You can't marry a bot!")

        val dbUser = DatabaseWrapper.getUserSafe(member.user)

        if (dbUser.marriedUserId != 0L)
            return ctx.send("${member.effectiveName} is already married to ${ctx.jda.getUserById(dbUser.marriedUserId).name}!")

        val yesEmote = "\u2705"
        val noEmote = "\u274E"

        ctx.channel.sendMessage("${member.user.asMention}, ${ctx.member!!.effectiveName} wants to marry you! What's your answer?").queue({
            it.addReaction(yesEmote).queue()
            it.addReaction(noEmote).queue()

            val msg = it

            EventListener.instance.waiter.await<MessageReactionAddEvent>(1, 60000L) {
                if (it.user.id == member.user.id && it.messageId == msg.id) {
                    when (it.reactionEmote.name) {
                        yesEmote -> {
                            asyncTransaction(Akatsuki.instance.pool) {
                                Users.update({
                                    Users.id.eq(ctx.author.idLong)
                                }) {
                                    it[marriedUserId] = member.user.idLong
                                }

                                Users.update({
                                    Users.id.eq(member.user.idLong)
                                }) {
                                    it[marriedUserId] = ctx.author.idLong
                                }

                                ctx.send("${ctx.member.effectiveName} is now married to ${member.effectiveName}! \uD83D\uDC96")
                            }.execute()
                        }

                        noEmote ->
                            ctx.send("${member.effectiveName} declined! \uD83D\uDC94")

                        else -> return@await false
                    }

                    true
                } else
                    false
            }
        })
    }
}