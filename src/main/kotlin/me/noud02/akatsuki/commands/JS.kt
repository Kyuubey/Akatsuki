/*
 *  Copyright (c) 2017 Noud Kerver
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Argument
import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.music.MusicManager
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