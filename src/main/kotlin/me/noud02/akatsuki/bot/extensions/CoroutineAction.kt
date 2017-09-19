package me.noud02.akatsuki.bot.extensions

import kotlinx.coroutines.experimental.future.await
import net.dv8tion.jda.core.requests.RestAction

suspend fun<V> RestAction<V>.await() = submit().await()
