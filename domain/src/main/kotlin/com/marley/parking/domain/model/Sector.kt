package com.marley.parking.domain.model

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.OccupancyRate
import com.marley.parking.domain.model.vo.SectorName

class Sector(
    val id: Long? = null,
    val name: SectorName,
    val basePrice: Money,
    val maxCapacity: Int
) {
    fun occupancyRate(currentOccupied: Int): OccupancyRate =
        OccupancyRate(currentOccupied.toDouble() / maxCapacity)

    fun isFull(currentOccupied: Int): Boolean = currentOccupied >= maxCapacity
}
