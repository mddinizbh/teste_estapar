package com.marley.parking.domain.port.outbound

import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.vo.SectorName

interface SectorRepository {
    fun findByName(name: SectorName): Sector?
    fun findAll(): List<Sector>
    fun save(sector: Sector): Sector
    fun saveAll(sectors: List<Sector>)
}
