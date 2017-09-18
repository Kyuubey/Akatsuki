package me.noud02.akatsuki.bot.entities

import kotlinx.coroutines.experimental.async

open class AsyncCommand {
    val subcommands = mutableMapOf<String, Command>()
    open val name = "awoo"
    open val desc = "awoo~"
    open val ownerOnly = false
    open val noHelp = false
    open val guildOnly = false

    open suspend fun asyncRun(ctx: Context) = ctx.send("Empty command")

    fun run(ctx: Context) {
        async(ctx.client.coroutineDispatcher) {
            try {
                asyncRun(ctx)
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }
    }

    fun addSubcommand(cmd: Command) {
        subcommands[cmd.name] = cmd
    }
}