package com.marley.parking.adapter.outbound.persistence.repository

import com.marley.parking.adapter.outbound.persistence.entity.SpotEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.Optional

@Repository
interface SpotMicronautRepository : CrudRepository<SpotEntity, Long> {
    fun findByLatAndLng(lat: Double, lng: Double): Optional<SpotEntity>
    fun findFirstBySectorIdAndOccupiedFalse(sectorId: Long): Optional<SpotEntity>
    fun countBySectorIdAndOccupiedTrue(sectorId: Long): Long
}
