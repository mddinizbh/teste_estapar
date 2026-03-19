package com.marley.parking.adapter.outbound.persistence.adapter

import com.marley.parking.adapter.outbound.persistence.mapper.PersistenceMapper
import com.marley.parking.adapter.outbound.persistence.repository.SectorMicronautRepository
import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.SectorRepository
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class SectorRepositoryAdapter(
    private val sectorMicronautRepository: SectorMicronautRepository
) : SectorRepository {

    override fun findByName(name: SectorName): Sector? {
        return sectorMicronautRepository.findByName(name.value)
            .map { PersistenceMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findAll(): List<Sector> {
        return sectorMicronautRepository.findAll().map { PersistenceMapper.toDomain(it) }
    }

    override fun save(sector: Sector): Sector {
        val entity = PersistenceMapper.toEntity(sector)
        val saved = sectorMicronautRepository.save(entity)
        return PersistenceMapper.toDomain(saved)
    }

    override fun saveAll(sectors: List<Sector>) {
        sectors.forEach { sector ->
            try {
                val existing = findByName(sector.name)
                if (existing != null) {
                    log.info("Sector '{}' already exists, skipping", sector.name.value)
                    return@forEach
                }
                save(sector)
                log.info("Sector '{}' inserted successfully", sector.name.value)
            } catch (e: Exception) {
                log.warn("Failed to insert sector '{}': {}", sector.name.value, e.message)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SectorRepositoryAdapter::class.java)
    }
}
