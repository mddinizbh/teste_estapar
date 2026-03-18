package com.marley.parking.domain.port.inbound

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import java.time.LocalDate

interface RevenueQueryUseCase {
    fun execute(sectorName: SectorName, date: LocalDate): Money
}
