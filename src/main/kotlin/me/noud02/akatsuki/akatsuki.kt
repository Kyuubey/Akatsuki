@file:JvmName("Akatsuki")
package me.noud02.akatsuki

import me.noud02.akatsuki.bot.Akatsuki

fun main (args: Array<String>) {
    val token = (if (args.isEmpty())
        System.getenv("AKATSUKI_TOKEN")
    else
        args[0]) ?: return println("No token specified!")

    val db_name: String = System.getenv("AKATSUKI_DB_NAME") ?: return println("No database name specified")
    val db_user: String = System.getenv("AKATSUKI_DB_USER") ?: ""
    val db_pass: String = System.getenv("AKATSUKI_DB_PASS") ?: ""

    val bot = Akatsuki(token, db_name, db_user, db_pass)

    bot.addOwner(System.getenv("AKATSUKI_OWNER_ID") ?: "")
    bot.setPrefix("awoo!")
    bot.setGame("awoo~ | ${bot.jda.guilds.size} Guild(s)!")
}
