package com.marley.parking.adapter.inbound.rest

import com.marley.parking.adapter.inbound.dto.WebhookEventDto
import com.marley.parking.adapter.inbound.mapper.EventMapper
import com.marley.parking.domain.port.inbound.VehicleEntryUseCase
import com.marley.parking.domain.port.inbound.VehicleExitUseCase
import com.marley.parking.domain.port.inbound.VehicleParkedUseCase
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory

@Controller("/webhook")
class WebhookController(
    private val vehicleEntryUseCase: VehicleEntryUseCase,
    private val vehicleParkedUseCase: VehicleParkedUseCase,
    private val vehicleExitUseCase: VehicleExitUseCase
) {
    private val log = LoggerFactory.getLogger(WebhookController::class.java)

    @Post
    fun handleEvent(@Body event: WebhookEventDto): HttpResponse<Unit> {
        try {
            when (event.event_type) {
                "ENTRY" -> vehicleEntryUseCase.execute(
                    EventMapper.toLicensePlate(event.license_plate),
                    EventMapper.toInstant(event.entry_time)
                )
                "PARKED" -> vehicleParkedUseCase.execute(
                    EventMapper.toLicensePlate(event.license_plate),
                    event.lat ?: throw IllegalArgumentException("lat is required"),
                    event.lng ?: throw IllegalArgumentException("lng is required")
                )
                "EXIT" -> vehicleExitUseCase.execute(
                    EventMapper.toLicensePlate(event.license_plate),
                    EventMapper.toInstant(event.exit_time)
                )
                else -> log.warn("Unknown event type: {}", event.event_type)
            }
        } catch (e: Exception) {
            log.error("Error processing webhook event: {}", event.event_type, e)
        }
        return HttpResponse.ok()
    }
}
