package com.marley.parking.config

import com.marley.parking.domain.port.outbound.GarageConfigLoader
import com.marley.parking.domain.port.outbound.SectorRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class StartupLoader(
    private val garageConfigLoader: GarageConfigLoader,
    private val sectorRepository: SectorRepository,
    private val spotRepository: SpotRepository
) : ApplicationEventListener<ServerStartupEvent> {

    private val log = LoggerFactory.getLogger(StartupLoader::class.java)

    override fun onApplicationEvent(event: ServerStartupEvent) {
        try {
            log.info("Loading garage configuration from simulator...")
            val config = garageConfigLoader.loadConfig()

            log.info("Persisting {} sectors and {} spots", config.sectors.size, config.spots.size)
            sectorRepository.saveAll(config.sectors)
            spotRepository.saveAll(config.spots)

            log.info("Garage configuration loaded successfully")
        } catch (e: Exception) {
            log.error("Failed to load garage configuration", e)
        }
    }
}
