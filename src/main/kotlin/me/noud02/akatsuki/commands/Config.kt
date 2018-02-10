/*
 *   Copyright (c) 2017-2018 Noud Kerver
 *
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation
 *   files (the "Software"), to deal in the Software without
 *   restriction, including without limitation the rights to use,
 *   copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following
 *   conditions:
 *
 *   The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *   OTHER DEALINGS IN THE SOFTWARE.
 */

package me.noud02.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.Akatsuki
import me.noud02.akatsuki.EventListener
import me.noud02.akatsuki.annotations.*
import me.noud02.akatsuki.db.schema.Guilds
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.extensions.searchRoles
import me.noud02.akatsuki.extensions.searchTextChannels
import me.noud02.akatsuki.utils.RolePicker
import me.noud02.akatsuki.utils.TextChannelPicker
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.TextChannel
import org.jetbrains.exposed.sql.update

@Arguments(
        Argument("option", "string"),
        Argument("value", "any")
)
@Perm(Permission.MANAGE_SERVER)
class Set : Command() {
    private val yes = listOf(
            "y",
            "yes",
            "true",
            "enable",
            "enabled"
    )

    private val no = listOf(
            "n",
            "no",
            "false",
            "disable",
            "disabled"
    )

    override val desc = "Set something in the config"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val key = (ctx.args["option"] as String).toLowerCase()
        val value = (ctx.args["value"] as String).toLowerCase()

        asyncTransaction(Akatsuki.instance.pool) {
            when (key) {
                "forcelang" -> {
                    if (!yes.contains(value) && !no.contains(value))
                        return@asyncTransaction ctx.send("Invalid value: $value")

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[forceLang] = yes.contains(value)
                    }

                    ctx.send("Set `forceLang` to `${yes.contains(value)}`")
                }

                "lang" -> {
                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[lang] = ctx.args["value"] as String
                    }

                    ctx.send("Set `lang` to `${ctx.args["value"]}`")
                }

                "logs" -> {
                    if (!yes.contains(value) && !no.contains(value))
                        return@asyncTransaction ctx.send("Invalid value: $value")

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[logs] = yes.contains(value)
                    }

                    ctx.send("Set `logs` to `${yes.contains(value)}`")
                }

                "starboard" -> {
                    if (!yes.contains(value) && !no.contains(value))
                        return@asyncTransaction ctx.send("Invalid value: $value")

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[starboard] = yes.contains(value)
                    }

                    ctx.send("Set `starboard` to `${yes.contains(value)}`")
                }

                "starboardchannel" -> {
                    val channels = ctx.guild!!.searchTextChannels(value)

                    if (channels.isEmpty())
                        return@asyncTransaction ctx.send("Couldn't find that channel!")

                    fun updateChannel(channel: TextChannel) {
                        Guilds.update({
                            Guilds.id.eq(ctx.guild.idLong)
                        }) {
                            it[starboardChannel] = channel.idLong
                        }

                        ctx.send("Set `starboardChannel` to ${channel.asMention}")
                    }

                    if (channels.size > 1)
                        TextChannelPicker(
                                EventListener.instance.waiter,
                                ctx.member!!,
                                channels,
                                ctx.guild
                        )
                                .build(ctx.channel)
                                .thenAccept {
                                    updateChannel(it)
                                }
                    else
                        updateChannel(channels[0])
                }

                "modlogs" -> {
                    if (!yes.contains(value) && !no.contains(value))
                        return@asyncTransaction ctx.send("Invalid value: $value")

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[modlogs] = yes.contains(value)
                    }

                    ctx.send("Set `modlogs` to `${yes.contains(value)}`")
                }

                "modlogchannel" -> {
                    val channels = ctx.guild!!.searchTextChannels(value)

                    if (channels.isEmpty())
                        return@asyncTransaction ctx.send("Couldn't find that channel!")

                    fun updateChannel(channel: TextChannel) {
                        Guilds.update({
                            Guilds.id.eq(ctx.guild.idLong)
                        }) {
                            it[modlogChannel] = channel.idLong
                        }

                        ctx.send("Set `modlogChannel` to ${channel.asMention}")
                    }

                    if (channels.size > 1)
                        TextChannelPicker(
                                EventListener.instance.waiter,
                                ctx.member!!,
                                channels,
                                ctx.guild
                        )
                                .build(ctx.channel)
                                .thenAccept {
                                    updateChannel(it)
                                }
                    else
                        updateChannel(channels[0])
                }

                "welcome" -> {
                    if (!yes.contains(value) && !no.contains(value))
                        return@asyncTransaction ctx.send("Invalid value: $value")

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[welcome] = yes.contains(value)
                    }

                    ctx.send("Set `welcome` to `${yes.contains(value)}`")
                }

                "welcomechannel" -> {
                    val channels = ctx.guild!!.searchTextChannels(value)

                    if (channels.isEmpty())
                        return@asyncTransaction ctx.send("Couldn't find that channel!")

                    fun updateChannel(channel: TextChannel) {
                        Guilds.update({
                            Guilds.id.eq(ctx.guild.idLong)
                        }) {
                            it[welcomeChannel] = channel.idLong
                        }

                        ctx.send("Set `welcomeChannel` to ${channel.asMention}")
                    }

                    if (channels.size > 1)
                        TextChannelPicker(
                                EventListener.instance.waiter,
                                ctx.member!!,
                                channels,
                                ctx.guild
                        )
                                .build(ctx.channel)
                                .thenAccept {
                                    updateChannel(it)
                                }
                    else
                        updateChannel(channels[0])
                }

                "welcomemessage" -> {
                    val text = ctx.args["value"] as String

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[welcomeMessage] = text
                    }

                    ctx.send("Set `welcomeMessage` to \"$text\"")
                }

                "leavemessage" -> {
                    val text = ctx.args["value"] as String

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[leaveMessage] = text
                    }

                    ctx.send("Set `leaveMessage` to \"$text\"")
                }

                "levelmessages" -> {
                    if (!yes.contains(value) && !no.contains(value))
                        return@asyncTransaction ctx.send("Invalid value: $value")

                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[levelMessages] = yes.contains(value)
                    }

                    ctx.send("Set `levelMessages` to `${yes.contains(value)}`")
                }

                "mutedrole" -> {
                    val roles = ctx.guild!!.searchRoles(value)

                    if (roles.isEmpty())
                        return@asyncTransaction ctx.send("Couldn't find that role!")

                    fun updateRole(role: Role) {
                        Guilds.update({
                            Guilds.id.eq(ctx.guild.idLong)
                        }) {
                            it[mutedRole] = role.idLong
                        }

                        ctx.send("Set `mutedRole` to ${role.name}")
                    }

                    if (roles.size > 1)
                        RolePicker(
                                EventListener.instance.waiter,
                                ctx.member!!,
                                roles,
                                ctx.guild
                        )
                                .build(ctx.channel)
                                .thenAccept {
                                    updateRole(it)
                                }
                    else
                        updateRole(roles[0])
                }

                else -> return@asyncTransaction ctx.send("Invalid key: $key")
            }
        }.execute()
    }
}

@Load
@Alias("cfg", "conf", "settings")
class Config : Command() {
    override val guildOnly = true

    init {
        addSubcommand(Set(), "set")
    }

    override fun run(ctx: Context) = ctx.send("""```ini
[Guild Config]
# To set something in the config use: 'config set <option> <value>'
# Example: 'config set logs true'

# Modlogs
modlogs         = ${ctx.storedGuild!!.modlogs}
modlogChannel   = ${
    if (ctx.storedGuild.modlogChannel != null)
        "#${ctx.guild!!.getTextChannelById(ctx.storedGuild.modlogChannel)?.name ?: "unknown"}"
    else
        "null"
    }

# Starboard
starboard       = ${ctx.storedGuild.starboard}
starboardChannel= ${
    if (ctx.storedGuild.starboardChannel != null)
        "#${ctx.guild!!.getTextChannelById(ctx.storedGuild.starboardChannel)?.name ?: "unknown"}"
    else
        "null"
    }

# Logs
logs            = ${ctx.storedGuild.logs}

# Locale
lang            = ${ctx.storedGuild.lang}
forceLang       = ${ctx.storedGuild.forceLang}

# Welcome
welcome         = ${ctx.storedGuild.welcome}
welcomeChannel  = ${
    if (ctx.storedGuild.welcomeChannel != null)
        "#${ctx.guild!!.getTextChannelById(ctx.storedGuild.welcomeChannel)?.name ?: "unknown"}"
    else
        "null"
    }

welcomeMessage  = "${ctx.storedGuild.welcomeMessage}"
leaveMessage    = "${ctx.storedGuild.leaveMessage}"

# Other
levelMessages   = ${ctx.storedGuild.levelMessages}```""")
}