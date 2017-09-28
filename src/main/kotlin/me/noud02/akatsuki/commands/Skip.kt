package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.entities.Perm
import me.noud02.akatsuki.bot.i18n
import me.noud02.akatsuki.bot.music.MusicManager
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member

@Load
@Perm(Permission.MANAGE_SERVER, true)
class Skip : Command() {
    override val name = "skip"
    override val desc = "Skips the current song"
    override val guildOnly = true

    // TODO add thing so you cant skip while there's nothing in the queue

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel())
            return ctx.send(i18n.parse(ctx.lang.getString("join_voice_channel_fail"), mapOf("username" to ctx.author.name))) // TODO rename translation
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connected!")

        if (ctx.perms["MANAGE_SERVER"] == true) {
            manager.scheduler.next()
            ctx.send(i18n.parse(ctx.lang.getString("force_skip"), mapOf("username" to ctx.author.name)))
        } else {
            val members = manager.voiceChannel.members.filter { member: Member -> !member.user.isBot }

            if (members.size - 1 <= manager.voteSkip.size) {
                manager.scheduler.next()
                return ctx.send(ctx.lang.getString("voteskip_success"))
            }

            if (manager.voteSkip.contains(ctx.author.id))
                return ctx.send(ctx.lang.getString("already_voted"))

            if (members.size - 1 <= manager.voteSkip.size + 1) {
                manager.scheduler.next()
                return ctx.send(ctx.lang.getString("voteskip_success"))
            }

            manager.voteSkip.add(ctx.author.id)
            ctx.send(i18n.parse(ctx.lang.getString("voteskip_add_success"), mapOf("votes" to manager.voteSkip.size, "total_votes" to members.size - 1)))
        }
    }
}