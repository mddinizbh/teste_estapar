package com.marley.parking.adapter.inbound.rest

import com.marley.parking.adapter.inbound.dto.WebhookEventDto
import com.marley.parking.adapter.inbound.dto.WebhookEventType
import com.marley.parking.adapter.inbound.logging.LogContext
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
    @LogContext
    open fun handleEvent(@Body @Valid event: WebhookEventDto): HttpResponse<Unit> {
        logger.info { "Webhook recebido | raw_entry_time=${event.entry_time}, raw_exit_time=${event.exit_time}" }

        val eventType = try {
            WebhookEventType.valueOf(event.event_type)
        } catch (e: IllegalArgumentException) {
            logger.warn { "Tipo de evento desconhecido: ${event.event_type}" }
            return HttpResponse.ok()
        }

        when (eventType) {
            WebhookEventType.ENTRY -> {
                val parsedEntryTime = EventMapper.toInstant(event.entry_time)
                logger.info { "ENTRY timestamp parsed | raw=${event.entry_time}, parsed=$parsedEntryTime" }
                vehicleEntryUseCase.execute(
                    EventMapper.toLicensePlate(event.license_plate),
                    parsedEntryTime
                )
            }
            WebhookEventType.PARKED -> vehicleParkedUseCase.execute(
                EventMapper.toLicensePlate(event.license_plate),
                Coordinates(
                    lat = event.lat ?: throw IllegalArgumentException("lat is required"),
                    lng = event.lng ?: throw IllegalArgumentException("lng is required")
                )
            )
            WebhookEventType.EXIT -> {
                val parsedExitTime = EventMapper.toInstant(event.exit_time)
                logger.info { "EXIT timestamp parsed | raw=${event.exit_time}, parsed=$parsedExitTime" }
                vehicleExitUseCase.execute(
                    EventMapper.toLicensePlate(event.license_plate),
                    parsedExitTime
                )
            }
        }

        logger.info { "Webhook processado" }
        return HttpResponse.ok()
    }
}
