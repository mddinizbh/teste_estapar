package com.marley.parking.adapter.outbound.http

import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.GarageConfig
import com.marley.parking.domain.port.outbound.GarageConfigLoader
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
class GarageConfigLoaderAdapter(
    private val garageSimClient: GarageSimClient
) : GarageConfigLoader {

    override fun loadConfig(): GarageConfig {
        val response = garageSimClient.getGarageConfig()

        val sectors = response.garage.map { sectorData ->
            Sector(
                name = SectorName(sectorData.sector),
                basePrice = Money(BigDecimal.valueOf(sectorData.base_price)),
                maxCapacity = sectorData.max_capacity
            )
        }

        val spots = response.spots.map { spotData ->
            Spot(
                id = spotData.id,
                sectorName = SectorName(spotData.sector),
                coordinates = Coordinates(spotData.lat, spotData.lng)
            )
        }

        return GarageConfig(sectors = sectors, spots = spots)
    }
}
