package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import org.python.util.PythonInterpreter

@Load
@Argument("code", "string")
class Py : Command() {
    override val name = "py"
    override val desc = "Evaluate (python) code"
    override val ownerOnly = true

    override fun run(ctx: Context) {
        System.setProperty("python.import.site", "false")
        val py = PythonInterpreter()
        py.set("ctx", ctx)
        try {
            val res = py.exec(ctx.rawArgs.joinToString(" "))
            ctx.sendCode("py", res)
            py.set("ctx", null)
        } catch(e: Throwable) {
            ctx.sendCode("diff", "- ${e.message}")
        }
    }
}