package me.noud02.akatsuki.bot.extensions

import com.sun.xml.internal.ws.util.CompletedFuture
import kotlinx.coroutines.experimental.future.await
import net.dv8tion.jda.core.requests.RestAction
import java.util.concurrent.CompletableFuture

suspend fun<V> RestAction<V>.await(): V {
    // As `RestAction#submit()` already returns a subclass of CompletableFuture
    // we can just cast to it
    val fut = submit()
    if (fut is CompletedFuture<V>) return fut.get()
    return (fut as CompletableFuture<V>).await()
}