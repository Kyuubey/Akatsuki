package me.noud02.akatsuki.bot.entities

import kotlinx.coroutines.experimental.async

open class AsyncCommand : Command() {
    open suspend fun asyncRun(ctx: Context) = ctx.send("Empty command")

    override fun run(ctx: Context) {
        async(ctx.client.coroutineDispatcher) {
            try {
                asyncRun(ctx)
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }
    }

    fun addCommand(cmd: AsyncCommand) {
        subcommands[cmd.name] = cmd
    }
}