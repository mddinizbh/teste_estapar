package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.inbound.RevenueQueryUseCase
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Singleton
class RevenueQueryUseCaseImpl(
    private val parkingSessionRepository: ParkingSessionRepository
) : RevenueQueryUseCase {

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun execute(sectorName: SectorName, date: LocalDate): Money {
        logger.info { "Consultando receita | setor=${sectorName.value}, data=$date" }

        val revenue = parkingSessionRepository.sumChargedBySectorAndDate(sectorName, date)

        logger.info { "Resultado da receita | setor=${sectorName.value}, valor=${revenue.value}" }

        return revenue
    }
}
