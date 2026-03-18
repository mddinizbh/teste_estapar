package com.marley.parking.domain.port.outbound

import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot

data class GarageConfig(
    val sectors: List<Sector>,
    val spots: List<Spot>
)

interface GarageConfigLoader {
    fun loadConfig(): GarageConfig
}
