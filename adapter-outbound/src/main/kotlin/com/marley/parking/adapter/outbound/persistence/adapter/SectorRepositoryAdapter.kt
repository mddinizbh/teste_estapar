package com.marley.parking.adapter.outbound.persistence.adapter

import com.marley.parking.adapter.outbound.persistence.mapper.PersistenceMapper
import com.marley.parking.adapter.outbound.persistence.repository.SectorMicronautRepository
import com.marley.parking.adapter.outbound.persistence.repository.SpotMicronautRepository
import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.SectorRepository
import jakarta.inject.Singleton

@Singleton
class SectorRepositoryAdapter(
    private val sectorMicronautRepository: SectorMicronautRepository,
    private val spotMicronautRepository: SpotMicronautRepository
) : SectorRepository {

    override fun findByName(name: SectorName): Sector? {
        return sectorMicronautRepository.findByName(name.value)
            .map { PersistenceMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findWithAvailableCapacity(): Sector? {
        return sectorMicronautRepository.findAll().firstOrNull { entity ->
            val occupied = spotMicronautRepository.countBySectorIdAndOccupiedTrue(entity.id!!)
            occupied < entity.maxCapacity
        }?.let { PersistenceMapper.toDomain(it) }
    }

    override fun save(sector: Sector): Sector {
        val entity = PersistenceMapper.toEntity(sector)
        val saved = sectorMicronautRepository.save(entity)
        return PersistenceMapper.toDomain(saved)
    }

    override fun countOccupiedSpots(sectorName: SectorName): Int {
        val sector = sectorMicronautRepository.findByName(sectorName.value).orElse(null)
            ?: return 0
        return spotMicronautRepository.countBySectorIdAndOccupiedTrue(sector.id!!).toInt()
    }

    override fun saveAll(sectors: List<Sector>) {
        val entities = sectors.map { PersistenceMapper.toEntity(it) }
        sectorMicronautRepository.saveAll(entities)
    }
}
