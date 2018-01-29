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
import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.annotations.Argument
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.schema.Tags
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

@Load
@Argument("name", "string")
@Alias("deletepasta", "deleteahh")
class DeleteTag : Command() {
    override val desc = "Delete tags."

    override fun run(ctx: Context) {
        val name = ctx.args["name"] as String

        asyncTransaction(Akatsuki.instance.pool) {
            val tag = Tags.select { Tags.tagName.eq(name) }.firstOrNull()
                    ?: return@asyncTransaction ctx.send("That tag doesn't exist!")

            if (tag[Tags.ownerId] != ctx.author.idLong)
                return@asyncTransaction ctx.send("You don't own this tag!")

            Tags.deleteWhere { Tags.tagName.eq(name) }

            ctx.send("Deleted tag '$name'")
        }.execute()
    }
}