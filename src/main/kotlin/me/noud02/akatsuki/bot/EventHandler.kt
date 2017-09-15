package me.noud02.akatsuki.bot

import net.dv8tion.jda.core.events.guild.GuildJoinEvent

class EventHandler(private val client: Akatsuki) {
    fun guildJoin(event: GuildJoinEvent) {
        println("New guild: ${event.guild.name}")
    }
}