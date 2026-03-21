package com.marley.parking.adapter.inbound.rest

import com.marley.parking.adapter.inbound.dto.WebhookEventDto
import com.marley.parking.adapter.inbound.dto.WebhookEventType
import com.marley.parking.adapter.inbound.mapper.EventMapper
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.port.inbound.VehicleEntryUseCase
import com.marley.parking.domain.port.inbound.VehicleExitUseCase
import com.marley.parking.domain.port.inbound.VehicleParkedUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import jakarta.validation.Valid

private val logger = KotlinLogging.logger {}

@Controller("/webhook")
open class WebhookController(
    private val vehicleEntryUseCase: VehicleEntryUseCase,
    private val vehicleParkedUseCase: VehicleParkedUseCase,
    private val vehicleExitUseCase: VehicleExitUseCase
) {

    @Post
    open fun handleEvent(@Body @Valid event: WebhookEventDto): HttpResponse<Unit> {
        logger.info { "Webhook received | type=${event.event_type}, plate=${event.license_plate}" }

        val eventType = try {
            WebhookEventType.valueOf(event.event_type)
        } catch (e: IllegalArgumentException) {
            logger.warn { "Unknown event type: ${event.event_type}" }
            return HttpResponse.ok()
        }

        when (eventType) {
            WebhookEventType.ENTRY -> vehicleEntryUseCase.execute(
                EventMapper.toLicensePlate(event.license_plate),
                EventMapper.toInstant(event.entry_time)
            )
            WebhookEventType.PARKED -> vehicleParkedUseCase.execute(
                EventMapper.toLicensePlate(event.license_plate),
                Coordinates(
                    lat = event.lat ?: throw IllegalArgumentException("lat is required"),
                    lng = event.lng ?: throw IllegalArgumentException("lng is required")
                )
            )
            WebhookEventType.EXIT -> vehicleExitUseCase.execute(
                EventMapper.toLicensePlate(event.license_plate),
                EventMapper.toInstant(event.exit_time)
            )
        }

        logger.info { "Webhook processed | type=${event.event_type}, plate=${event.license_plate}" }
        return HttpResponse.ok()
    }
}
