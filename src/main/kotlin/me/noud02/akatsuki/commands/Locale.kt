package me.noud02.akatsuki.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import me.noud02.akatsuki.bot.entities.*
import me.noud02.akatsuki.bot.i18n
import me.noud02.akatsuki.bot.schema.Users
import org.jetbrains.exposed.sql.update

@Alias("change")
@Argument("lang", "string")
class SetLocale : AsyncCommand() {
    override val name = "set"
    override val desc = "Set your language"

    override suspend fun asyncRun(ctx: Context) {
        asyncTransaction(ctx.client.pool) {
            Users.update({
                Users.id.eq(ctx.author.id)
            }) {
                it[lang] = ctx.args["lang"] as String
            }

            ctx.send(i18n.parse(ctx.lang.getString("language_set"), mapOf("language" to ctx.args["lang"] as String)))
        }.await()
    }
}

@Load
class Locale : Command() {
    override val name = "locale"
    override val desc = "View or change your language"

    init {
        addSubcommand(SetLocale())
    }

    override fun run(ctx: Context) = ctx.send(i18n.parse(ctx.lang.getString("language_check"), mapOf("language" to ctx.lang.locale)))
}