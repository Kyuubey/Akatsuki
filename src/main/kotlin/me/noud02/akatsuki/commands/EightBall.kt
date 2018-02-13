/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.commands

import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load

@Load
class EightBall : Command() {
    override val name = "8ball"
    override val desc = "Magic 8 ball that answers your questions."

    override fun run(ctx: Context) {
        val resp = listOf(
                "It is certain",
                "Without a doubt",
                "You may rely on it",
                "Most likely..",
                "Yes",
                "No",
                "Signs point to yes",
                "Don't count on it",
                "My sources say no",
                "Very doubtful",
                "nope.avi",
                "*facepalm*",
                "Maybe?",
                "(╯°□°）╯︵ ┻━┻",
                "404 Answer not found",
                "Never",
                "Sure",
                "Do you honestly think I am gonna answer to that?",
                "*...*",
                "Might be possible",
                "Your face is the answer 🔥",
                "👀",
                "Kappa",
                "thonk"
        )
        
        ctx.send(resp[Math.floor(Math.random() * resp.size).toInt()])
    }
}