package com.marley.parking.adapter.inbound.rest

import com.marley.parking.adapter.inbound.dto.ErrorResponse
import com.marley.parking.domain.exception.DuplicateEntryException
import com.marley.parking.domain.exception.InvalidSessionStateException
import com.marley.parking.domain.exception.SectorFullException
import com.marley.parking.domain.exception.SpotAlreadyOccupiedException
import com.marley.parking.domain.exception.VehicleAlreadyParkedException
import com.marley.parking.domain.exception.VehicleNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error

private val logger = KotlinLogging.logger {}

@Controller
class GlobalExceptionHandler {

    @Error(global = true, exception = DuplicateEntryException::class)
    fun handleDuplicateEntry(request: HttpRequest<*>, e: DuplicateEntryException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Business exception: ${e.message}" }
        return HttpResponse.status<ErrorResponse>(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse("DUPLICATE_ENTRY", e.message ?: "Vehicle already has an active session"))
    }

    @Error(global = true, exception = SpotAlreadyOccupiedException::class)
    fun handleSpotOccupied(request: HttpRequest<*>, e: SpotAlreadyOccupiedException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Business exception: ${e.message}" }
        return HttpResponse.status<ErrorResponse>(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse("SPOT_OCCUPIED", e.message ?: "Spot is already occupied"))
    }

    @Error(global = true, exception = VehicleAlreadyParkedException::class)
    fun handleVehicleAlreadyParked(request: HttpRequest<*>, e: VehicleAlreadyParkedException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Business exception: ${e.message}" }
        return HttpResponse.status<ErrorResponse>(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse("VEHICLE_ALREADY_PARKED", e.message ?: "Vehicle is already parked"))
    }

    @Error(global = true, exception = SectorFullException::class)
    fun handleSectorFull(request: HttpRequest<*>, e: SectorFullException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Business exception: ${e.message}" }
        return HttpResponse.status<ErrorResponse>(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse("SECTOR_FULL", e.message ?: "All sectors are full"))
    }

    @Error(global = true, exception = InvalidSessionStateException::class)
    fun handleInvalidSessionState(request: HttpRequest<*>, e: InvalidSessionStateException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Business exception: ${e.message}" }
        return HttpResponse.status<ErrorResponse>(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse("INVALID_SESSION_STATE", e.message ?: "Invalid session state"))
    }

    @Error(global = true, exception = VehicleNotFoundException::class)
    fun handleVehicleNotFound(request: HttpRequest<*>, e: VehicleNotFoundException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Business exception: ${e.message}" }
        return HttpResponse.status<ErrorResponse>(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("VEHICLE_NOT_FOUND", e.message ?: "Vehicle not found"))
    }

    @Error(global = true, exception = IllegalArgumentException::class)
    fun handleValidationError(request: HttpRequest<*>, e: IllegalArgumentException): HttpResponse<ErrorResponse> {
        logger.warn(e) { "Validation error: ${e.message}" }
        return HttpResponse.badRequest(ErrorResponse("VALIDATION_ERROR", e.message ?: "Validation error"))
    }

    @Error(global = true, exception = Exception::class)
    fun handleUnexpected(request: HttpRequest<*>, e: Exception): HttpResponse<ErrorResponse> {
        logger.error(e) { "Unexpected error: ${e.message}" }
        return HttpResponse.serverError(ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
    }
}
