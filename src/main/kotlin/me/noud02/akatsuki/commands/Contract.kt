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
import me.noud02.akatsuki.EventListener
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.schema.Contracts
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.extensions.createContract
import me.noud02.akatsuki.utils.I18n
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.jetbrains.exposed.sql.select

@Argument("user", "user")
class ViewContract : Command() {
    override val desc = "View someone's contract!"

    override fun run(ctx: Context) {
        val member = ctx.args["user"] as Member

        asyncTransaction(Akatsuki.instance.pool) {
            val contract = Contracts.select { Contracts.userId.eq(member.user.idLong) }.firstOrNull()
                    ?: return@asyncTransaction ctx.send("That user hasn't made a contract with me (yet)!")

            ctx.send(EmbedBuilder().apply {
                setTitle("${member.user.name}'s Contract")
                descriptionBuilder.append("*${contract[Contracts.wish]}*")
                setFooter(contract[Contracts.date].toString(), null)
            }.build())
        }.execute()
    }
}

@Load
class Contract : Command() {
    override val desc = "\"I will make you a magical girl!\""

    init {
        addSubcommand(ViewContract(), "view")
    }

    override fun run(ctx: Context) {
        asyncTransaction(Akatsuki.instance.pool) {
            if (!Contracts.select { Contracts.userId.eq(ctx.author.idLong) }.empty())
                return@asyncTransaction ctx.send(
                        I18n.parse(
                                ctx.lang.getString("user_has_contract"),
                                mapOf("username" to ctx.author.name)
                        )
                )

            ctx.send(
                    I18n.parse(
                            ctx.lang.getString("ask_wish"),
                            mapOf("username" to ctx.author.name)
                    )
            )

            EventListener.instance.waiter.await<MessageReceivedEvent>(1, 60000L) {
                if (it.author.id == ctx.author.id && it.channel.id == ctx.channel.id) {
                    val ans = it.message.contentRaw

                    if (ans.toLowerCase().startsWith("cancel"))
                        return@await true

                    ctx.send(
                            I18n.parse(
                                    ctx.lang.getString("ask_sure_mg"),
                                    mapOf("username" to ctx.author.name)
                            )
                    )

                    EventListener.instance.waiter.await<MessageReceivedEvent>(1, 60000L) {
                        if (it.author.id == ctx.author.id && it.channel.id == ctx.channel.id) {
                            val ans2 = it.message.contentRaw

                            if (!ans2.toLowerCase().startsWith("y"))
                                return@await true

                            ctx.author.createContract(ans)

                            ctx.send(
                                    I18n.parse(
                                            ctx.lang.getString("mg_wish_granted"),
                                            mapOf("username" to ctx.author.name)
                                    )
                            )

                            true
                        } else
                            false
                    }

                    true
                } else
                    false
            }
        }.execute()
    }
}