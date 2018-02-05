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

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.annotations.*
import me.noud02.akatsuki.db.schema.Scripts
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.utils.LuaSandbox
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

@Perm(Permission.MANAGE_SERVER)
@Argument("script", "string")
class TestCommand : Command() {
    override val desc = "Test lua scripts"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val script = ctx.rawArgs.joinToString(" ")

        val result = LuaSandbox.eval(script, ctx.rawArgs)

        ctx.send(result)
    }
}

@Perm(Permission.MANAGE_SERVER)
@Arguments(
        Argument("name", "string"),
        Argument("script", "string")
)
@Flag("owner", 'o', "Make the custom command server owner only.")
class AddCommand : Command() {
    override val name = "Add custom commands"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val content = ctx.rawArgs.slice(1 until ctx.rawArgs.size).joinToString(" ")
        val name = ctx.args["name"] as String

        asyncTransaction(Akatsuki.instance.pool) {
            val scriptsMatch = Scripts.select { Scripts.guildId.eq(ctx.guild!!.idLong).and(Scripts.scriptName.eq(name)) }

            if (!scriptsMatch.empty())
                return@asyncTransaction ctx.send("Command with that name already exists!")

            Scripts.insert {
                it[script] = content
                it[scriptName] = name
                it[ownerId] = ctx.author.idLong
                it[ownerOnly] = ctx.flags.argMap.containsKey("owner") || ctx.flags.argMap.containsKey("o")
                it[guildId] = ctx.guild!!.idLong
            }

            ctx.send("Added custom command **$name**!")
        }.execute()
    }
}

@Perm(Permission.ADMINISTRATOR, true)
@Argument("name", "string")
class RemoveCommand : Command() {
    override val guildOnly = true
    override val desc = "Remove custom commands."

    override fun run(ctx: Context) {
        val name = ctx.args["name"]
        val hasPerm = ctx.perms["ADMINISTRATOR"] as Boolean

        asyncTransaction(Akatsuki.instance.pool) {
            val match = Scripts.select { Scripts.guildId.eq(ctx.guild!!.idLong).and(Scripts.scriptName.eq(name)) }.firstOrNull()
                    ?: return@asyncTransaction ctx.send("Couldn't find that command!")

            if (!hasPerm && match[Scripts.ownerId] != ctx.author.idLong)
                return@asyncTransaction ctx.send("You can't delete that command!")

            Scripts.deleteWhere {
                Scripts.scriptName.eq(name).and(Scripts.guildId.eq(ctx.guild!!.idLong))
            }

            ctx.send("Deleted command **${match[Scripts.scriptName]}**!")
        }.execute()
    }
}

@Load
@Arguments(
        Argument("command", "string"),
        Argument("arguments", "string", true)
)
@Alias("cmd", "script", "c", "s")
@Perm(Permission.ADMINISTRATOR, true)
class CustomCommand : Command() {
    override val desc = "Execute custom commands."
    override val guildOnly = true

    init {
        addSubcommand(TestCommand(), "test")
        addSubcommand(AddCommand(), "add")
        addSubcommand(RemoveCommand(), "remove")
    }

    override fun run(ctx: Context) {
        val cmd = ctx.args["command"] as String
        val hasPerm = ctx.perms["ADMINISTRATOR"] as Boolean

        asyncTransaction(Akatsuki.instance.pool) {
            val command = Scripts.select { Scripts.guildId.eq(ctx.guild!!.idLong).and(Scripts.scriptName.eq(cmd)) }.firstOrNull()
                    ?: return@asyncTransaction ctx.send("Couldn't find that command!")

            if (command[Scripts.ownerOnly] && !hasPerm)
                return@asyncTransaction ctx.send("This command is owner-only!")

            val res = LuaSandbox.eval(command[Scripts.script], ctx.rawArgs.slice(1 until ctx.rawArgs.size))

            ctx.send(res)
        }.execute()
    }
}