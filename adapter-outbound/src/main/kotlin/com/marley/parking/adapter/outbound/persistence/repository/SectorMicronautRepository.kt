package com.marley.parking.adapter.outbound.persistence.repository

import com.marley.parking.adapter.outbound.persistence.entity.SectorEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.Optional

@Repository
interface SectorMicronautRepository : CrudRepository<SectorEntity, Long> {
    fun findByName(name: String): Optional<SectorEntity>
}
