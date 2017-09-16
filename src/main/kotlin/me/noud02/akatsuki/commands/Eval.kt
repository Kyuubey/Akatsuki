package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import javax.script.ScriptEngineManager

@Argument("code", "string")
class Eval : Command() {
    override val name = "eval"
    override val desc = "Evaluate (js) code"
    override val ownerOnly = true

    override fun run(ctx: Context) {
        val engine = ScriptEngineManager().getEngineByName("nashorn")

        engine.put("ctx", ctx)

        try {
            val res = engine.eval(ctx.rawArgs.joinToString(" "))
            ctx.sendCode("js", res ?: "null")
        } catch (e: Throwable) {
            return ctx.sendCode("diff", "- ${e.message}")
        }

    }
}