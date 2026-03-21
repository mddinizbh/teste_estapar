package com.marley.parking.integration

import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.GarageConfig
import com.marley.parking.domain.port.outbound.GarageConfigLoader
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
@Replaces(GarageConfigLoader::class)
class TestGarageConfigLoader : GarageConfigLoader {
    override fun loadConfig(): GarageConfig {
        return GarageConfig(
            sectors = listOf(
                Sector(
                    name = SectorName("A"),
                    basePrice = Money(BigDecimal("10.00")),
                    maxCapacity = 100
                )
            ),
            spots = listOf(
                Spot(
                    id = 1L,
                    sectorName = SectorName("A"),
                    coordinates = Coordinates(-23.5505, -46.6333)
                ),
                Spot(
                    id = 2L,
                    sectorName = SectorName("A"),
                    coordinates = Coordinates(-23.5506, -46.6334)
                )
            )
        )
    }
}
