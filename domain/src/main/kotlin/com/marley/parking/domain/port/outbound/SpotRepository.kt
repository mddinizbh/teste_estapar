package com.marley.parking.domain.port.outbound

import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.SectorName

interface SpotRepository {
    fun findById(id: Long): Spot?
    fun findByCoordinates(coordinates: Coordinates): Spot?
    fun findFirstAvailableBySector(sectorName: SectorName): Spot?
    fun save(spot: Spot): Spot
    fun saveAll(spots: List<Spot>)
}
