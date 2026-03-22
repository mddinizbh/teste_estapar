package com.marley.parking.adapter.inbound.rest

import com.marley.parking.adapter.inbound.dto.RevenueResponseDto
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.inbound.RevenueQueryUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Controller("/revenue")
class RevenueController(
    private val revenueQueryUseCase: RevenueQueryUseCase,
    private val clock: Clock
) {

    @Get
    fun getRevenue(@QueryValue date: String, @QueryValue sector: String): RevenueResponseDto {
        logger.info { "Consulta de receita | data=$date, setor=$sector" }

        val parsedDate = LocalDate.parse(date)
        val sectorName = SectorName(sector)
        val revenue = revenueQueryUseCase.execute(sectorName, parsedDate)

        return RevenueResponseDto(
            amount = revenue.value,
            timestamp = Instant.now(clock).toString()
        )
    }
}
