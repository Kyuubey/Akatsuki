package me.noud02.akatsuki.bot.entities

open class Command {
    val subcommands: MutableMap<String, Command> = mutableMapOf()
    val args: MutableList<Argument> = mutableListOf()
    open val name = "awoo"
    open val desc = "awoo~"
    open val ownerOnly = false

    open fun run(ctx: Context) {
        ctx.send("Empty command")
    }

    fun addSubcommand(cmd: Command) {
        subcommands[cmd.name] = cmd
    }

    fun setArgument(name: String, type: String, optional: Boolean = false) {
        args.add(Argument(name, type, optional))
    }
}