package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import org.jruby.embed.ScriptingContainer

@Load
@Argument("code", "string")
class Ruby : Command() {
    override val name = "ruby"
    override val desc = "Evaluate (ruby) code"
    override val ownerOnly = true

    override fun run(ctx: Context) {
        val rb = ScriptingContainer()
        rb.put("ctx", ctx)
        try {
            val res = rb.runScriptlet(ctx.rawArgs.joinToString(" "))
            rb.remove("ctx")
            ctx.sendCode("rb", res ?: "null")
        } catch (e: Throwable) {
            ctx.sendCode("diff", "- ${e.message}")
        }
    }
}