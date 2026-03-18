package com.marley.parking.adapter.inbound.rest

import com.marley.parking.adapter.inbound.dto.RevenueRequestDto
import com.marley.parking.adapter.inbound.dto.RevenueResponseDto
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.inbound.RevenueQueryUseCase
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.time.Instant
import java.time.LocalDate

@Controller("/revenue")
class RevenueController(
    private val revenueQueryUseCase: RevenueQueryUseCase
) {

    @Get
    fun getRevenue(@Body request: RevenueRequestDto): RevenueResponseDto {
        val date = LocalDate.parse(request.date)
        val sectorName = SectorName(request.sector)
        val revenue = revenueQueryUseCase.execute(sectorName, date)

        return RevenueResponseDto(
            amount = revenue.value,
            timestamp = Instant.now().toString()
        )
    }
}
