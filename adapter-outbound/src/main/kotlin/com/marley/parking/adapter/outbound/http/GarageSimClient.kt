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
    val garage: List<SectorData>,
    val spots: List<SpotData>
)

@Serdeable
data class SectorData(
    val sector: String,
    val base_price: Double,
    val max_capacity: Int,
    val open_hour: String? = null,
    val close_hour: String? = null,
    val duration_limit_minutes: Int? = null
)

@Serdeable
data class SpotData(
    val id: Long,
    val sector: String,
    val lat: Double,
    val lng: Double,
    val occupied: Boolean = false
)
