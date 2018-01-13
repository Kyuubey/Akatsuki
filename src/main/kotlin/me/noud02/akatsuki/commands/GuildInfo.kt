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

import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.db.DatabaseWrapper
import me.noud02.akatsuki.entities.Command
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission


@Load
@Alias("guild", "serverinfo", "server")
class GuildInfo : Command() {
    override val desc = "Get info about the guild."
    override val guildOnly = true

    override fun run(ctx: Context) {
        val guild = DatabaseWrapper.getGuildSafe(ctx.guild!!)
        ctx.send(EmbedBuilder().apply {
            setTitle("${ctx.guild.name} (${ctx.guild.id})")

            descriptionBuilder.append("**Owner:** ${ctx.guild.owner.user.name}#${ctx.guild.owner.user.discriminator}\n")
            descriptionBuilder.append("**Bots:** ${ctx.guild.members.filter { it.user.isBot }.size}\n")
            descriptionBuilder.append("**Users:** ${ctx.guild.members.filter { !it.user.isBot }.size}\n")
            descriptionBuilder.append("**Roles:** ${ctx.guild.roles.size - 1}\n")
            descriptionBuilder.append("**Region:** ${ctx.guild.region.getName()}\n")
            descriptionBuilder.append("**Language:** ${guild.lang}\n")

            if (guild.prefixes.isNotEmpty())
                descriptionBuilder.append("**Prefixes:** ${guild.prefixes.joinToString(", ")}\n")

            if (ctx.client.jda.shardInfo != null)
                descriptionBuilder.append("**Shard:** ${(ctx.guild.idLong shr 22) % ctx.client.jda.shardInfo.shardTotal}\n")

            descriptionBuilder.append("**Emotes:** ${ctx.guild.emotes.joinToString(" ") { it.asMention }}\n")
            descriptionBuilder.append("**Mods:**\n${ctx.guild.members.filter {
                !it.user.isBot && (it.isOwner ||
                                it.hasPermission(Permission.BAN_MEMBERS) ||
                                it.hasPermission(Permission.KICK_MEMBERS) ||
                                it.hasPermission(Permission.ADMINISTRATOR))
            }.joinToString("\n") {
                "${it.user.name}#${it.user.discriminator}${if (!it.nickname.isNullOrEmpty()) "(${it.nickname})" else ""}"
            }}")
        }.build())
    }
}