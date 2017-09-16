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