package me.noud02.akatsuki.commands

import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.annotations.Perm
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.music.MusicManager
import me.noud02.akatsuki.utils.I18n
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
class Stop : Command() {
    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connected!")

        manager.player.stopTrack()

        ctx.send(I18n.parse(ctx.lang.getString("song_stop_success"), mapOf("username" to ctx.author.name)))
    }
}