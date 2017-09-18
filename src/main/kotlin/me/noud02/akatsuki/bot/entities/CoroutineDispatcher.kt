package me.noud02.akatsuki.bot.entities

import kotlinx.coroutines.experimental.CoroutineDispatcher
import java.util.concurrent.ExecutorService
import kotlin.coroutines.experimental.CoroutineContext

class CoroutineDispatcher(val pool: ExecutorService) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        pool.execute(block)
    }
}