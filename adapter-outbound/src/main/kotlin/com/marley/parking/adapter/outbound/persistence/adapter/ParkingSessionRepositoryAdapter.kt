package com.marley.parking.adapter.outbound.persistence.adapter

import com.marley.parking.adapter.outbound.persistence.PersistenceExceptionUtils
import com.marley.parking.adapter.outbound.persistence.mapper.PersistenceMapper
import com.marley.parking.adapter.outbound.persistence.repository.ParkingSessionMicronautRepository
import com.marley.parking.adapter.outbound.persistence.repository.SectorMicronautRepository
import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.ParkingStatus
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.exception.DuplicateEntryException
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset

private val logger = KotlinLogging.logger {}

@Singleton
class ParkingSessionRepositoryAdapter(
    private val parkingSessionMicronautRepository: ParkingSessionMicronautRepository,
    private val sectorMicronautRepository: SectorMicronautRepository
) : ParkingSessionRepository {

    override fun save(session: ParkingSession): ParkingSession {
        val sectorEntity = sectorMicronautRepository.findByName(session.sectorName.value).orElse(null)
            ?: throw IllegalStateException("Sector ${session.sectorName.value} not found")

        val saved = try {
            if (session.id == null) {
                val entity = PersistenceMapper.toEntity(session, sectorEntity.id!!)
                parkingSessionMicronautRepository.save(entity)
            } else {
                val existing = parkingSessionMicronautRepository.findById(session.id!!).orElseThrow {
                    IllegalStateException("ParkingSession ${session.id} not found")
                }
                existing.spotId = session.spotId
                existing.parkedTime = session.parkedTime
                existing.exitTime = session.exitTime
                existing.amountCharged = session.amountCharged?.value
                existing.status = session.status.name
                parkingSessionMicronautRepository.update(existing)
            }
        } catch (e: Exception) {
            if (PersistenceExceptionUtils.isUniqueConstraintViolation(e, "uq_active_session")) {
                logger.warn { "DB constraint blocked duplicate active session | plate=${session.licensePlate.value}" }
                throw DuplicateEntryException("Vehicle ${session.licensePlate.value} already has an active session")
            }
            if (PersistenceExceptionUtils.isOptimisticLockException(e)) {
                throw DuplicateEntryException("Session ${session.id} was modified concurrently")
            }
            throw e
        }

        return PersistenceMapper.toDomain(saved, session.sectorName.value)
    }

    override fun findActiveByPlate(plate: LicensePlate): ParkingSession? {
        return parkingSessionMicronautRepository
            .findByLicensePlateAndStatusIn(plate.value, ParkingStatus.ACTIVE_STATUSES.map { it.name })
            .map { entity ->
                val sectorName = sectorMicronautRepository.findById(entity.sectorId)
                    .map { it.name }.orElse("")
                PersistenceMapper.toDomain(entity, sectorName)
            }
            .orElse(null)
    }

    override fun countActiveBySector(sectorName: SectorName): Int {
        val sectorEntity = sectorMicronautRepository.findByName(sectorName.value).orElse(null)
            ?: return 0
        return parkingSessionMicronautRepository.countActiveBySectorId(sectorEntity.id!!).toInt()
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
