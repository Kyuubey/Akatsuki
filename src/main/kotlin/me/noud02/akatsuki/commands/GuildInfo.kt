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

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.annotations.Alias
import me.noud02.akatsuki.entities.AsyncCommand
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.annotations.Load
import me.noud02.akatsuki.schema.Guilds
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.select

@Load
@Alias("guild", "serverinfo", "server")
class GuildInfo : AsyncCommand() {
    override val desc = "Get info about the guild."
    override val guildOnly = true

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
            val embed = EmbedBuilder()
            val desc = embed.descriptionBuilder
            val guild = Guilds.select { Guilds.id.eq(ctx.guild!!.id) }.first()

            embed.setTitle(ctx.guild!!.name)

            desc.append("**Owner:** ${ctx.guild.owner.user.name}#${ctx.guild.owner.user.discriminator}\n")
            desc.append("**Bots:** ${ctx.guild.members.filter { it.user.isBot }.size}\n")
            desc.append("**Users:** ${ctx.guild.members.filter { !it.user.isBot }.size}\n")
            desc.append("**Roles:** ${ctx.guild.roles.size - 1}\n")
            desc.append("**Region:** ${ctx.guild.region.getName()}\n")
            desc.append("**Language:** ${guild[Guilds.lang]}\n")
            if (guild[Guilds.prefixes].isNotEmpty())
                desc.append("**Prefixes:** ${guild[Guilds.prefixes].joinToString(", ")}\n")

            if (ctx.client.jda.shardInfo != null)
                desc.append("**Shard:** ${(ctx.guild.idLong shr 22) % ctx.client.jda.shardInfo.shardTotal}\n")
            desc.append("**Emotes:** ${ctx.guild.emotes.joinToString(" ") { it.asMention }}\n")
            desc.append("**Mods:**\n${ctx.guild.members.filter {
                !it.user.isBot && (it.isOwner || it.hasPermission(Permission.BAN_MEMBERS) || it.hasPermission(Permission.KICK_MEMBERS) || it.hasPermission(Permission.ADMINISTRATOR))
            }.joinToString("\n") {
                "${it.user.name}#${it.user.discriminator}${if (!it.nickname.isNullOrEmpty()) "(${it.nickname})" else ""}"
            }}")

            embed.setDescription(desc)

            ctx.send(embed.build())
        }.await()
    }
}