package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load
import me.noud02.akatsuki.bot.music.MusicManager

@Load
class Join : Command() {
    override val name = "join"
    override val desc = "Have the bot join a voice channel"
    override val guildOnly = true

    override fun run(ctx: Context) {
        if (MusicManager.musicManagers.contains(ctx.guild?.id))
            return ctx.send("Already connected!")
        if (!ctx.member!!.voiceState.inVoiceChannel())
            return ctx.send("You must be in a voice channel to use this command!")
        MusicManager.join(ctx)
        ctx.send("Connected!")
    }
}