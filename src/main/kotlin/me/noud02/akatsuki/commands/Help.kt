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

import me.noud02.akatsuki.EventListener
import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.utils.I18n

@Load
@Alias("--help", "-h")
@Argument("command", "string", true)
class Help : Command() {
    override val desc = "Sends you help!"

    override fun run(ctx: Context) {
        if ("command" in ctx.args) {
            val cmd = ctx.args["command"] as String
            if (cmd !in EventListener.cmdHandler.commands) {
                ctx.send(
                        I18n.parse(
                                ctx.lang.getString("command_not_found"),
                                mapOf("username" to ctx.author.name)
                        )
                )
            } else {
                ctx.send(EventListener.cmdHandler.help(cmd))
            }
        } else {
            val commands = EventListener.cmdHandler.commands
                    .toSortedMap()
                    .map {
                        "\t${it.key}" + " ".repeat(20 - it.key.length) + it.value.desc
                    }

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

            if (part.isNotBlank() && part.split("\n").size < partSize) {
                parts.add(part)
            }

            for (partt in parts) {
                ctx.author.openPrivateChannel().complete().sendMessage("```$partt```").queue()
            }

            ctx.send(I18n.parse(ctx.lang.getString("help_message"), mapOf("username" to ctx.author.name)))
        }
    }
}