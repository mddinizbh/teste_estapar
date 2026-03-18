package com.marley.parking.adapter.outbound.persistence.adapter

import com.marley.parking.adapter.outbound.persistence.entity.ParkingSessionEntity
import com.marley.parking.adapter.outbound.persistence.mapper.PersistenceMapper
import com.marley.parking.adapter.outbound.persistence.repository.ParkingSessionMicronautRepository
import com.marley.parking.adapter.outbound.persistence.repository.SectorMicronautRepository
import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset

@Singleton
class ParkingSessionRepositoryAdapter(
    private val parkingSessionMicronautRepository: ParkingSessionMicronautRepository,
    private val sectorMicronautRepository: SectorMicronautRepository
) : ParkingSessionRepository {

    override fun save(session: ParkingSession): ParkingSession {
        val sectorEntity = sectorMicronautRepository.findByName(session.sectorName.value).orElse(null)
            ?: throw IllegalStateException("Sector ${session.sectorName.value} not found")

        val entity = PersistenceMapper.toEntity(session, sectorEntity.id!!)

        val saved = if (entity.id == null) {
            parkingSessionMicronautRepository.save(entity)
        } else {
            parkingSessionMicronautRepository.update(entity)
        }

        return PersistenceMapper.toDomain(saved, session.sectorName.value)
    }

    override fun findActiveByPlate(plate: LicensePlate): ParkingSession? {
        return parkingSessionMicronautRepository
            .findByLicensePlateAndStatusIn(plate.value, listOf("ENTERED", "PARKED"))
            .map { entity ->
                val sectorName = sectorMicronautRepository.findById(entity.sectorId)
                    .map { it.name }.orElse("")
                PersistenceMapper.toDomain(entity, sectorName)
            }
            .orElse(null)
    }

    override fun sumChargedBySectorAndDate(sectorName: SectorName, date: LocalDate): Money {
        val sectorEntity = sectorMicronautRepository.findByName(sectorName.value).orElse(null)
            ?: return Money(BigDecimal.ZERO)

        val startTime = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endTime = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val total = parkingSessionMicronautRepository.sumChargedBySectorAndDateRange(
            sectorEntity.id!!, startTime, endTime
        )
        return Money(total)
    }
}
