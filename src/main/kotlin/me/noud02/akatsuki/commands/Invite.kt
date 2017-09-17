package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.Command
import me.noud02.akatsuki.bot.entities.Context
import me.noud02.akatsuki.bot.entities.Load

@Load
class Invite : Command() {
    override val name = "invite"
    override val desc = "Get my invite!"

    override fun run(ctx: Context) = ctx.send("https://discordapp.com/oauth2/authorize?client_id=${ctx.client.jda.selfUser.id}&scope=bot&permissions=3468486")
}