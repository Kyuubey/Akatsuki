package me.noud02.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.EventListener
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.schema.Logs
import me.noud02.akatsuki.entities.AsyncCommand
import me.noud02.akatsuki.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

@Load
class Snipe : AsyncCommand() {
    override val guildOnly = true

    override fun run(ctx: Context) {
        asyncTransaction(Akatsuki.instance.pool) {
            val snipe = EventListener.instance.snipes.remove(ctx.channel.idLong)

            if (snipe != null) {
                val log = Logs.select {
                    Logs.guildId.eq(ctx.guild!!.idLong) and Logs.messageId.eq(snipe)
                }.first()

                val embed = EmbedBuilder().apply {
                    setAuthor("${log[Logs.authorName]}#${log[Logs.authorDiscrim]}", null, log[Logs.authorAvatar])
                    descriptionBuilder.append(log[Logs.content])
                    setFooter("Sniped by ${ctx.author.name}#${ctx.author.discriminator}", null)
                }

                ctx.send(embed.build())
            } else
                ctx.send("No snipes found!")
        }.execute()
    }
}