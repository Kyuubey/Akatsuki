package me.noud02.akatsuki.bot.entities

open class Command {
    val subcommands = mutableMapOf<String, Command>()
    open val name = "awoo"
    open val desc = "awoo~"
    open val ownerOnly = false
    open val noHelp = false
    open val guildOnly = false

    open fun run(ctx: Context) = ctx.send("Empty command")

    fun addSubcommand(cmd: Command) {
        subcommands[cmd.name] = cmd
    }
}