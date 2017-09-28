package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.*
import me.noud02.akatsuki.bot.i18n

@Load
@Alias("--help", "-h")
@Argument("command", "string", true)
class Help : Command() {
    override val name = "help"
    override val desc = "Sends you help!"

    override fun run(ctx: Context) {
        if (ctx.args.contains("command"))
            return ctx.send(ctx.help(ctx.args["command"] as String))
        else {
            val commands: List<String> = ctx.client.cmdHandler.commands.toSortedMap().map { entry: Map.Entry<String, Command> -> "\t${entry.value.name}" + " ".repeat(20 - entry.value.name.length) + entry.value.desc }
            val text = "Flags:\n\n\t-h, --help${" ".repeat(10)}Get help on a command!\n\nCommands:\n\n${commands.joinToString("\n")}"
            val partSize = 40
            val parts = mutableListOf<String>()
            val lines = text.split("\n")
            var part = ""

            for (line in lines) {
                if (part.split("\n").size >= partSize) {
                    parts.add(part)
                    part = ""
                }

                part += "$line\n"
            }

            if (part.isNotBlank() && part.split("\n").size < partSize)
                parts.add(part)

            for (partt in parts) {
                ctx.author.openPrivateChannel().complete().sendMessage("```$partt```").queue()
            }

            ctx.send(i18n.parse(ctx.lang.getString("help_message"), mapOf("username" to ctx.author.name)))
        }
    }
}