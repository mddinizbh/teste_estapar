package com.marley.parking.adapter.outbound.http

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable

@Client("\${garage-simulator.base-url}")
interface GarageSimClient {

    @Get("/garage")
    fun getGarageConfig(): GarageConfigResponse
}

@Serdeable
data class GarageConfigResponse(
    val garage: GarageData
)

@Serdeable
data class GarageData(
    val sectors: List<SectorData>
)

@Serdeable
data class SectorData(
    val sector: String,
    val basePrice: Double,
    val maxCapacity: Int,
    val spots: List<SpotData>
)

@Serdeable
data class SpotData(
    val id: Long,
    val lat: Double,
    val lng: Double
)
