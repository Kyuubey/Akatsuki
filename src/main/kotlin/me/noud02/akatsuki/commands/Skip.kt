package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.entities.Perm
import me.noud02.akatsuki.bot.music.MusicManager
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member

@Load
@Perm(Permission.MANAGE_SERVER, true)
class Skip : Command() {
    override val name = "skip"
    override val desc = "Skips the current song"
    override val guildOnly = true

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel())
            return ctx.send("You must be in a voicechannel to execute this command!")
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connected!")

        if (ctx.perms["MANAGE_SERVER"] == true) {
            manager.scheduler.next()
            ctx.send("Force-skipped current song!")
        } else {
            val members = manager.voiceChannel.members.filter { member: Member -> !member.user.isBot }

            if (members.size - 1 <= manager.voteSkip.size) {
                manager.scheduler.next()
                return ctx.send("Voteskip succeeded!")
            }

            if (manager.voteSkip.contains(ctx.author.id))
                return ctx.send("You already voted to skip!")

            if (members.size - 1 <= manager.voteSkip.size + 1) {
                manager.scheduler.next()
                return ctx.send("Voteskip succeeded!")
            }

            manager.voteSkip.add(ctx.author.id)
            ctx.send("Added your vote! [${manager.voteSkip.size}/${members.size - 1}]")
        }
    }
}