package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import javax.script.ScriptEngineManager

@Load
@Argument("code", "string")
class JS : Command() {
    override val name = "js"
    override val desc = "Evaluate (js) code"
    override val ownerOnly = true

    override fun run(ctx: Context) {
        val engine = ScriptEngineManager().getEngineByName("nashorn")

        engine.put("ctx", ctx)

        try {
            val res = engine.eval(ctx.rawArgs.joinToString(" "))
            ctx.sendCode("js", res ?: "null")
        } catch (e: Throwable) {
            ctx.sendCode("diff", "- ${e.message}")
        }

    }
}