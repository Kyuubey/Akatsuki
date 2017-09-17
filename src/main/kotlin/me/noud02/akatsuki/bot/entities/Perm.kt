package me.noud02.akatsuki.bot.entities

import net.dv8tion.jda.core.Permission

annotation class Perm(val name: Permission, val optional: Boolean = false)

annotation class Perms(val perms: Array<Perm>)