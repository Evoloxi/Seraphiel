package me.evo.seraphiel

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import me.evo.seraphiel.command.CommandRegistry
import me.evo.seraphiel.hook.ChatHook
import me.evo.seraphiel.listener.GameStartListener
import me.evo.seraphiel.listener.PlayerJoinListener
import net.minecraft.client.Minecraft
import net.weavemc.loader.api.ModInitializer
import net.weavemc.loader.api.event.EventBus
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeSource

@Suppress("unused")
class Seraphiel : ModInitializer {

    override fun init() {
        EventBus.subscribe(GameStartListener)
        EventBus.subscribe(PlayerJoinListener)
        EventBus.subscribe(ChatHook)
        CommandRegistry
        println("Init!")
    }

    object IO : CoroutineScope {
        private val threadPool = Executors.newFixedThreadPool(16) as ThreadPoolExecutor
        private val dispatcher = threadPool.asCoroutineDispatcher()
        override val coroutineContext = dispatcher + SupervisorJob() + CoroutineName("GABRIEL_IO")
    }

    companion object : CoroutineScope {
        private val threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor
        private val dispatcher = threadPool.asCoroutineDispatcher()
        override val coroutineContext: CoroutineContext = dispatcher + SupervisorJob() + CoroutineName("GABRIEL")

        val LOGGER: Logger = LogManager.getLogger("Seraphiel")
        val mc: Minecraft by lazy { Minecraft.getMinecraft() }
        val NOW = TimeSource.Monotonic.markNow()
    }
}