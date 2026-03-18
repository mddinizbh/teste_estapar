package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.inbound.RevenueQueryUseCase
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import java.time.LocalDate

class RevenueQueryUseCaseImpl(
    private val parkingSessionRepository: ParkingSessionRepository
) : RevenueQueryUseCase {

    override fun execute(sectorName: SectorName, date: LocalDate): Money {
        return parkingSessionRepository.sumChargedBySectorAndDate(sectorName, date)
    }
}
