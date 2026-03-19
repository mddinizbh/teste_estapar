package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.inbound.RevenueQueryUseCase
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Singleton
class RevenueQueryUseCaseImpl(
    private val parkingSessionRepository: ParkingSessionRepository
) : RevenueQueryUseCase {

    override fun execute(sectorName: SectorName, date: LocalDate): Money {
        logger.info { "Querying revenue | sector=${sectorName.value}, date=$date" }

        val revenue = parkingSessionRepository.sumChargedBySectorAndDate(sectorName, date)

        logger.info { "Revenue result | sector=${sectorName.value}, amount=${revenue.value}" }

        return revenue
    }
}
