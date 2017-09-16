package me.noud02.akatsuki.bot.entities

open class Command {
    val subcommands: MutableMap<String, Command> = mutableMapOf()
    open val name = "awoo"
    open val desc = "awoo~"
    open val ownerOnly = false

    open fun run(ctx: Context) {
        ctx.send("Empty command")
    }

    fun addSubcommand(cmd: Command) {
        subcommands[cmd.name] = cmd
    }
}