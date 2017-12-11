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

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.entities.*
import me.noud02.akatsuki.utils.I18n
import me.noud02.akatsuki.db.schema.Users
import org.jetbrains.exposed.sql.update

@Alias("change")
@Argument("lang", "string")
class SetLocale : AsyncCommand() {
    override val name = "set"
    override val desc = "Set your language"

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
            Users.update({
                Users.id.eq(ctx.author.id)
            }) {
                it[lang] = ctx.args["lang"] as String
            }

            ctx.send(I18n.parse(ctx.lang.getString("language_set"), mapOf("language" to ctx.args["lang"] as String)))
        }.await()
    }
}

@Load
class Locale : Command() {
    override val desc = "View or change your language"

    init {
        addSubcommand(SetLocale())
    }

    override fun run(ctx: Context) = ctx.send(I18n.parse(ctx.lang.getString("language_check"), mapOf("language" to ctx.lang.locale)))
}