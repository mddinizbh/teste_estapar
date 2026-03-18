package com.marley.parking.adapter.outbound.persistence.adapter

import com.marley.parking.adapter.outbound.persistence.mapper.PersistenceMapper
import com.marley.parking.adapter.outbound.persistence.repository.SectorMicronautRepository
import com.marley.parking.adapter.outbound.persistence.repository.SpotMicronautRepository
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.SpotRepository
import jakarta.inject.Singleton

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
        val sectorEntity = sectorMicronautRepository.findByName(spot.sectorName.value).orElse(null)
            ?: throw IllegalStateException("Sector ${spot.sectorName.value} not found")
        val entity = PersistenceMapper.toEntity(spot, sectorEntity)
        val saved = spotMicronautRepository.update(entity)
        return PersistenceMapper.toDomain(saved)
    }

    override fun saveAll(spots: List<Spot>) {
        spots.forEach { spot ->
            val sectorEntity = sectorMicronautRepository.findByName(spot.sectorName.value).orElse(null)
                ?: throw IllegalStateException("Sector ${spot.sectorName.value} not found")
            val entity = PersistenceMapper.toEntity(spot, sectorEntity)
            spotMicronautRepository.save(entity)
        }
    }
}
