package me.noud02.akatsuki.bot.entities

@Repeatable
annotation class Argument(val name: String, val type: String, val optional: Boolean = false)

annotation class Arguments(val args: Array<Argument>)