package com.marley.parking.config

import com.marley.parking.domain.port.outbound.GarageConfigLoader
import com.marley.parking.domain.port.outbound.SectorRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class StartupLoader(
    private val garageConfigLoader: GarageConfigLoader,
    private val sectorRepository: SectorRepository,
    private val spotRepository: SpotRepository
) : ApplicationEventListener<ServerStartupEvent> {

    companion object {
        private const val MAX_RETRIES = 3
        private val BACKOFF_DELAYS_MS = longArrayOf(1_000, 3_000, 5_000)
    }

    override fun onApplicationEvent(event: ServerStartupEvent) {
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                logger.info { "Loading garage configuration from simulator (attempt $attempt/$MAX_RETRIES)..." }
                val config = garageConfigLoader.loadConfig()

                logger.info { "Persisting ${config.sectors.size} sectors and ${config.spots.size} spots" }
                sectorRepository.saveAll(config.sectors)
                spotRepository.saveAll(config.spots)

                logger.info { "Garage configuration loaded successfully" }
                return
            } catch (e: Exception) {
                lastException = e
                logger.warn(e) { "Failed to load garage configuration (attempt $attempt/$MAX_RETRIES)" }

                if (attempt < MAX_RETRIES) {
                    val delay = BACKOFF_DELAYS_MS[attempt - 1]
                    logger.info { "Retrying in ${delay}ms..." }
                    Thread.sleep(delay)
                }
            }
        }

        throw IllegalStateException(
            "Failed to load garage configuration after $MAX_RETRIES attempts. Application cannot start.",
            lastException
        )
    }
}
