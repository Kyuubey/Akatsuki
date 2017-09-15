package me.noud02.akatsuki

class Command {
    val subcommands: MutableMap<String, Command> = mutableMapOf()

    fun run(ctx: Context) {
        ctx.send("Empty command")
    }

    fun setSubcommand(name: String, cmd: Command) {
        this.subcommands[name] = cmd
    }
}