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
import me.noud02.akatsuki.annotations.Arguments
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.db.schema.Modlogs
import me.noud02.akatsuki.entities.AsyncCommand
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.extensions.await
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

@Load
@Perm(Permission.MANAGE_SERVER)
@Arguments(
        Argument("case", "string"),
        Argument("reason", "string")
)
class Reason : AsyncCommand() {
    override val guildOnly = true

    override suspend fun asyncRun(ctx: Context) {
        val reasonArg = ctx.args["reason"] as String

        if (reasonArg.length > 512)
            return ctx.send("Reason is longer than 512 characters")

        val caseArg = (ctx.args["case"] as String).toLowerCase()

        asyncTransaction(Akatsuki.instance.pool) {
            val cases = Modlogs.select { Modlogs.guildId eq ctx.guild!!.idLong }
            val caseIds: List<Int> = when (caseArg) {
                "l" -> listOf(cases.count())
                else -> {
                    if (caseArg.toIntOrNull() == null) {
                        if (!caseArg.matches("\\d+\\.\\.\\d+".toRegex()))
                            return@asyncTransaction

                        val first = caseArg.split("..")[0].toInt()
                        val second = caseArg.split("..")[1].toInt()

                        if (first > second)
                            return@asyncTransaction ctx.send("First number ($first) is larger than second number ($second)")

                        var list = listOf<Int>()

                        for (i in first..second) {
                            list += i
                        }

                        list
                    } else
                        listOf(caseArg.toInt())
                }
            }

            for (id in caseIds) {
                Modlogs.update({ Modlogs.guildId.eq(ctx.guild!!.idLong) and Modlogs.caseId.eq(id) }) { it[reason] = reasonArg }
                val log = cases.firstOrNull { it[Modlogs.caseId] == id }

                if (log != null)
                    ctx.guild!!
                            .getTextChannelById(ctx.storedGuild!!.modlogChannel)
                            .getMessageById(log[Modlogs.messageId])
                            .queue ({
                                it.editMessage(
                                        it.contentRaw.replace(
                                                "\\*\\*Reason\\*\\*: .+\n\\*\\*Responsible moderator\\*\\*: .+".toRegex(),
                                                "**Reason**: $reasonArg\n" +
                                                        "**Responsible moderator**: ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})"
                                        )
                                ).queue()
                            }) {
                                ctx.sendError(it)
                                it.printStackTrace()
                            }
            }

            ctx.send("\uD83D\uDC4C")
        }.await()
    }
}