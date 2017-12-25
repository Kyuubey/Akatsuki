package me.noud02.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.annotations.*
import me.noud02.akatsuki.db.schema.Guilds
import me.noud02.akatsuki.entities.AsyncCommand
import me.noud02.akatsuki.entities.Command
import me.noud02.akatsuki.entities.Context
import me.noud02.akatsuki.utils.TextChannelPicker
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import org.jetbrains.exposed.sql.update

@Arguments(
        Argument("key", "string"),
        Argument("value", "any")
)
@Perm(Permission.MANAGE_SERVER)
class Set : AsyncCommand() {
    private val yes = listOf(
            "y",
            "yes",
            "true"
    )

    private val no = listOf(
            "n",
            "no",
            "false"
    )

    override val guildOnly = true

    override suspend fun asyncRun(ctx: Context) {
        val key = (ctx.args["key"] as String).toLowerCase()
        val value = (ctx.args["value"] as String).toLowerCase()

        asyncTransaction(ctx.client.pool) {
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
                    val channel: TextChannel = when {
                        "<#\\d+>".toRegex().matches(value) -> try {
                            ctx.guild!!.getTextChannelById("<#(\\d+)>".toRegex().matchEntire(value)?.groupValues?.get(1))
                        } catch (e: Throwable) {
                            return@asyncTransaction ctx.send("Couldn't find that channel!")
                        }

                        ctx.guild!!.getTextChannelsByName(value, true).isNotEmpty() -> {
                            val channels = ctx.guild.getTextChannelsByName(value, true)

                            if (channels.size > 1) {
                                val picker = TextChannelPicker(ctx.client.waiter, ctx.member!!, channels, ctx.guild)

                                picker.build(ctx.channel).get()
                            } else
                                channels[0]
                        }

                        value.toLongOrNull() != null && ctx.guild.getTextChannelById(value) != null -> ctx.guild.getTextChannelById(value)

                        else -> return@asyncTransaction ctx.send("Couldn't find that channel!")
                    }

                    Guilds.update({
                        Guilds.id.eq(ctx.guild.idLong)
                    }) {
                        it[starboardChannel] = channel.idLong
                    }

                    ctx.send("Set `starboardChannel` to ${channel.asMention}")
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
                    val channel: TextChannel = when {
                        "<#\\d+>".toRegex().matches(value) -> try {
                            ctx.guild!!.getTextChannelById("<#(\\d+)>".toRegex().matchEntire(value)?.groupValues?.get(1))
                        } catch (e: Throwable) {
                            return@asyncTransaction ctx.send("Couldn't find that channel!")
                        }

                        ctx.guild!!.getTextChannelsByName(value, true).isNotEmpty() -> {
                            val channels = ctx.guild.getTextChannelsByName(value, true)

                            if (channels.size > 1) {
                                val picker = TextChannelPicker(ctx.client.waiter, ctx.member!!, channels, ctx.guild)

                                picker.build(ctx.channel).get()
                            } else
                                channels[0]
                        }

                        value.toLongOrNull() != null && ctx.guild.getTextChannelById(value) != null -> ctx.guild.getTextChannelById(value)

                        else -> return@asyncTransaction ctx.send("Couldn't find that channel!")
                    }

                    Guilds.update({
                        Guilds.id.eq(ctx.guild.idLong)
                    }) {
                        it[modlogChannel] = channel.idLong
                    }

                    ctx.send("Set `modlogChannel` to ${channel.asMention}")
                }

                else -> return@asyncTransaction ctx.send("Invalid key: $key")
            }
        }.await()
    }
}

@Load
@Alias("cfg", "conf")
class Config : Command() {
    override val guildOnly = true

    init {
        addSubcommand(Set(), "set")
    }

    override fun run(ctx: Context) = ctx.send("""```ini
[Guild Config]
# Modlogs
modlogs = ${ctx.storedGuild!!.modlogs}
modlogChannel = #${ctx.guild!!.getTextChannelById(ctx.storedGuild.modlogChannel).name}

# Starboard
starboard = ${ctx.storedGuild.starboard}
starboardChannel = #${ctx.guild.getTextChannelById(ctx.storedGuild.starboardChannel).name}

# Logs
logs = ${ctx.storedGuild.logs}

# Locale
lang = ${ctx.storedGuild.lang}
forceLang = ${ctx.storedGuild.forceLang}```""")
}