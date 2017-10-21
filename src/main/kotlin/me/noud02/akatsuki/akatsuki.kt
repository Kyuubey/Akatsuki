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

    // bot.buildSharded(3)
    bot.build()
    bot.addOwner(System.getenv("AKATSUKI_OWNER_ID") ?: "")
    bot.addPrefix("awoo!")
    bot.setGame("awoo~ | ${bot.jda!!.guilds.size} Guild(s)!")
}
