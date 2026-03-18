package com.marley.parking.adapter.outbound.persistence.repository

import com.marley.parking.adapter.outbound.persistence.entity.ParkingSessionEntity
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

@Repository
interface ParkingSessionMicronautRepository : CrudRepository<ParkingSessionEntity, Long> {

    fun findByLicensePlateAndStatusIn(licensePlate: String, statuses: List<String>): Optional<ParkingSessionEntity>

    @Query("SELECT COALESCE(SUM(ps.amountCharged), 0) FROM ParkingSessionEntity ps WHERE ps.sectorId = :sectorId AND ps.status = 'EXITED' AND ps.exitTime >= :startTime AND ps.exitTime < :endTime")
    fun sumChargedBySectorAndDateRange(sectorId: Long, startTime: Instant, endTime: Instant): BigDecimal
}
