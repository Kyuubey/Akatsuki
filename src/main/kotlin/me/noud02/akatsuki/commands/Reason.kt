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