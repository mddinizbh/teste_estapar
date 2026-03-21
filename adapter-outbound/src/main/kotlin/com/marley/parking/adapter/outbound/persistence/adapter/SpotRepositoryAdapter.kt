package com.marley.parking.adapter.outbound.persistence.adapter

import com.marley.parking.adapter.outbound.persistence.PersistenceExceptionUtils
import com.marley.parking.adapter.outbound.persistence.mapper.PersistenceMapper
import com.marley.parking.adapter.outbound.persistence.repository.SectorMicronautRepository
import com.marley.parking.adapter.outbound.persistence.repository.SpotMicronautRepository
import com.marley.parking.domain.exception.SpotAlreadyOccupiedException
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.SpotRepository
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class SpotRepositoryAdapter(
    private val spotMicronautRepository: SpotMicronautRepository,
    private val sectorMicronautRepository: SectorMicronautRepository
) : SpotRepository {

    override fun findById(id: Long): Spot? {
        return spotMicronautRepository.findById(id)
            .map { PersistenceMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByCoordinates(coordinates: Coordinates): Spot? {
        return spotMicronautRepository.findByLatAndLng(coordinates.lat, coordinates.lng)
            .map { PersistenceMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findFirstAvailableBySector(sectorName: SectorName): Spot? {
        val sector = sectorMicronautRepository.findByName(sectorName.value).orElse(null)
            ?: return null
        return spotMicronautRepository.findFirstBySectorIdAndOccupiedFalse(sector.id!!)
            .map { PersistenceMapper.toDomain(it) }
            .orElse(null)
    }

    override fun save(spot: Spot): Spot {
        val entity = spotMicronautRepository.findById(spot.id).orElseThrow {
            IllegalStateException("Spot ${spot.id} not found")
        }
        entity.occupied = spot.isOccupied
        val saved = try {
            spotMicronautRepository.update(entity)
        } catch (e: Exception) {
            if (PersistenceExceptionUtils.isOptimisticLockException(e)) {
                throw SpotAlreadyOccupiedException("Spot ${spot.id} was modified concurrently")
            }
            throw e
        }
        return PersistenceMapper.toDomain(saved)
    }

    override fun saveAll(spots: List<Spot>) {
        spots.forEach { spot ->
            try {
                val existing = findByCoordinates(spot.coordinates)
                if (existing != null) {
                    log.info("Spot at ({}, {}) already exists, skipping", spot.coordinates.lat, spot.coordinates.lng)
                    return@forEach
                }
                val sectorEntity = sectorMicronautRepository.findByName(spot.sectorName.value).orElse(null)
                    ?: throw IllegalStateException("Sector ${spot.sectorName.value} not found")
                val entity = PersistenceMapper.toEntity(spot, sectorEntity)
                spotMicronautRepository.save(entity)
                log.info("Spot at ({}, {}) inserted successfully", spot.coordinates.lat, spot.coordinates.lng)
            } catch (e: Exception) {
                log.warn("Failed to insert spot at ({}, {}): {}", spot.coordinates.lat, spot.coordinates.lng, e.message)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SpotRepositoryAdapter::class.java)
    }
}
