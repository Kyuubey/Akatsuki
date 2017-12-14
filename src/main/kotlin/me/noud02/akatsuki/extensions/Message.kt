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

package me.noud02.akatsuki.extensions

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.db.schema.Logs
import org.jetbrains.exposed.sql.insert
import com.google.gson.Gson
import net.dv8tion.jda.core.entities.Message
import org.json.JSONObject


fun Message.log(ev: String = "CREATE") {
    val gson = Gson()

    asyncTransaction(Akatsuki.client.pool) {
        Logs.insert {
            it[event] =  ev
            it[messageId] = idLong
            it[content] = contentRaw
            it[attachments] = this@log.attachments.map { it.url }.toTypedArray()
            it[embeds] = this@log.embeds.map { JSONObject(gson.toJson(it)) }.toTypedArray()
            it[authorId] = author.idLong
            it[authorName] = author.name
            it[authorDiscrim] = author.discriminator
            it[authorAvatar] = author.avatarUrl
            it[authorNick] = member?.nickname ?: ""
            it[guildId] = guild.idLong
        }
    }.execute().get()
}