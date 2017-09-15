@file:JvmName("Akatsuki")
package me.noud02.akatsuki

import me.noud02.akatsuki.bot.Akatsuki

fun main (args: Array<String>) {
    val token = (if (args.isEmpty())
        System.getenv()["AKATSUKI_TOKEN"]
    else
        args[0]) ?: return println("No token specified!")

    val bot = Akatsuki(token)

    bot.setPrefix("awoo!")
    bot.setGame("awoo~")
}
