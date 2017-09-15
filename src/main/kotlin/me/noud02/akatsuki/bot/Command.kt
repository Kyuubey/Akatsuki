package me.noud02.akatsuki.bot

open class Command {
    val subcommands: MutableMap<String, Command> = mutableMapOf()
    val args: Array<Argument> = arrayOf()

    open fun run(ctx: Context) {
        ctx.send("Empty command")
    }

    fun setSubcommand(name: String, cmd: Command) {
        subcommands[name] = cmd
    }

    fun setArgument(name: String, type: String, optional: Boolean = false) {
        args.plusElement(Argument(name, type, optional))
    }
}