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

package me.noud02.akatsuki.commands

import me.noud02.akatsuki.bot.entities.*
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import java.time.format.DateTimeFormatter

@Load
@Alias("user")
@Argument("user", "user", true)
class UserInfo : Command() {
    override val name = "userinfo"
    override val desc = "Get info on a user."
    override val guildOnly: Boolean = true

    override fun run(ctx: Context) {
        val member = ctx.args["user"] as? Member ?: ctx.member!!
        val embed = EmbedBuilder()
        val desc = embed.descriptionBuilder
        
        val statusEmote =if (member.game.type == Game.GameType.STREAMING) 
            "<:streaming:313956277132853248>" 
        else 
            when (member.onlineStatus.name) {
            "ONLINE" -> "<:online:313956277808005120>"
            "OFFLINE" -> "<:offline:313956277237710868>"
            "IDLE" -> "<:away:313956277220802560>"
            else -> "<:invisible:313956277107556352>"
        }
        
        embed.setTitle(
                "$statusEmote ${member.user.name}#${member.user.discriminator}${
                if (!member.nickname.isNullOrEmpty()) 
                    " (${member.nickname})" 
                else 
                    ""
                }"
        )
        
        embed.setThumbnail(member.user.avatarUrl)
        
        desc.append("**Playing:** ${member.game.name}")
        
        embed.setDescription(desc)
        
        embed.setFooter(
                "Joined Discord on ${
                member.user.creationTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
                }, ${ctx.guild!!.name} on ${
                member.joinDate.format(DateTimeFormatter.RFC_1123_DATE_TIME)
                }",
                null
        )
        
        ctx.send(embed.build())
    }
}