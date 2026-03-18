package com.marley.parking.domain.port.outbound

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import java.time.LocalDate

interface ParkingSessionRepository {
    fun save(session: ParkingSession): ParkingSession
    fun findActiveByPlate(plate: LicensePlate): ParkingSession?
    fun sumChargedBySectorAndDate(sectorName: SectorName, date: LocalDate): Money
}
