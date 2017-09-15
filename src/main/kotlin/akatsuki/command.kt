package akatsuki

open class Command {
    val subcommands: MutableMap<String, Command> = mutableMapOf()

    open fun run(ctx: Context) {
        ctx.send("Empty command")
    }

    fun setSubcommand(name: String, cmd: Command) {
        this.subcommands[name] = cmd
    }
}