package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Alias
import me.noud02.akatsuki.bot.entities.AsyncCommand
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.extensions.await

@Load
@Alias("clear", "cls")
class Clean : AsyncCommand() {
    override val name = "clean"
    override val desc = "Clean the last 10 messages sent by me"
    override val guildOnly = true

    override suspend fun asyncRun(ctx: Context) {
        val msgs = ctx.channel.getHistoryAround(ctx.msg, 100).await()

        val botmsgs = msgs.retrievedHistory
                .filter { it.author.id == ctx.selfMember!!.user.id }
                .subList(0, 10)

        botmsgs
                .forEach { it.delete().await() }

        ctx.send("Cleaned ${botmsgs.size} messages!")
    }
}