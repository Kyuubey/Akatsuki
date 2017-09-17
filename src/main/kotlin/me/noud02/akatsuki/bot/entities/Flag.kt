package me.noud02.akatsuki.bot.entities

annotation class Flag(val flag: String, val abbr: Char, val desc: String)

annotation class Flags(val flags: Array<Flag>)