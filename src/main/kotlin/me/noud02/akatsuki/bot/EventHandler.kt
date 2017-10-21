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

package me.noud02.akatsuki.bot

import me.noud02.akatsuki.bot.schema.Guilds
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class EventHandler(private val client: Akatsuki) {

    private val loggr = LoggerFactory.getLogger(this::class.java)

    fun guildLeave(event: GuildLeaveEvent) {
        println("Left guild: ${event.guild.name}")
        transaction {
            try {
                Guilds.deleteWhere {
                    Guilds.id.eq(event.guild.id)
                }
            } catch (e: Throwable) {
                loggr.error("Error while trying to delete guild ${event.guild.name} from DB", e)
            }
        }
    }

    fun guildJoin(event: GuildJoinEvent) {
        println("New guild: ${event.guild.name}")
        transaction {
            try {
                Guilds.insert {
                    it[id] = event.guild.id
                    it[name] = event.guild.name
                    it[lang] = "en_US"
                    it[prefixes] = arrayOf()
                }
            } catch (e: Throwable) {
                loggr.error("Error while trying to insert guild ${event.guild.name} in DB", e)
            }
        }
    }
}