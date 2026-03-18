package com.marley.parking.adapter.outbound.persistence.mapper

import com.marley.parking.adapter.outbound.persistence.entity.ParkingSessionEntity
import com.marley.parking.adapter.outbound.persistence.entity.SectorEntity
import com.marley.parking.adapter.outbound.persistence.entity.SpotEntity
import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.ParkingStatus
import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.*
import java.math.BigDecimal

object PersistenceMapper {

    fun toDomain(entity: SectorEntity): Sector = Sector(
        id = entity.id,
        name = SectorName(entity.name),
        basePrice = Money(entity.basePrice),
        maxCapacity = entity.maxCapacity
    )

    fun toEntity(domain: Sector): SectorEntity = SectorEntity(
        id = domain.id,
        name = domain.name.value,
        basePrice = domain.basePrice.value,
        maxCapacity = domain.maxCapacity
    )

    fun toDomain(entity: SpotEntity): Spot = Spot(
        id = entity.id,
        sectorName = SectorName(entity.sector.name),
        coordinates = Coordinates(entity.lat, entity.lng),
        occupied = entity.occupied
    )

    fun toEntity(domain: Spot, sectorEntity: SectorEntity): SpotEntity = SpotEntity(
        id = domain.id,
        sector = sectorEntity,
        lat = domain.coordinates.lat,
        lng = domain.coordinates.lng,
        occupied = domain.isOccupied
    )

    fun toDomain(entity: ParkingSessionEntity, sectorName: String): ParkingSession = ParkingSession.reconstitute(
        id = entity.id!!,
        licensePlate = LicensePlate(entity.licensePlate),
        sectorName = SectorName(sectorName),
        spotId = entity.spotId,
        entryTime = entity.entryTime,
        parkedTime = entity.parkedTime,
        exitTime = entity.exitTime,
        priceAtEntry = Money(entity.priceAtEntry),
        amountCharged = entity.amountCharged?.let { Money(it) },
        status = ParkingStatus.valueOf(entity.status)
    )

    fun toEntity(domain: ParkingSession, sectorId: Long): ParkingSessionEntity = ParkingSessionEntity(
        id = domain.id,
        licensePlate = domain.licensePlate.value,
        sectorId = sectorId,
        spotId = domain.spotId,
        entryTime = domain.entryTime,
        parkedTime = domain.parkedTime,
        exitTime = domain.exitTime,
        priceAtEntry = domain.priceAtEntry.value,
        amountCharged = domain.amountCharged?.value,
        status = domain.status.name
    )
}
