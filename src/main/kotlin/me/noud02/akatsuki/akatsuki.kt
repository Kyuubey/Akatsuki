@file:JvmName("Akatsuki")
package me.noud02.akatsuki

import me.noud02.akatsuki.bot.Akatsuki

fun main (args: Array<String>) {

    if (args.isEmpty())
        throw Error("Token not specified!")

    val bot = Akatsuki(args[0])

    bot.setPrefix("awoo!")
    bot.setGame("awoo~")
}
