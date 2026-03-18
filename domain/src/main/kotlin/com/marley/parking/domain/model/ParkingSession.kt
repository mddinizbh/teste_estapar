package com.marley.parking.domain.model

import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import java.time.Instant

class ParkingSession private constructor(
    val id: Long? = null,
    val licensePlate: LicensePlate,
    val sectorName: SectorName,
    spotId: Long? = null,
    val entryTime: Instant,
    parkedTime: Instant? = null,
    exitTime: Instant? = null,
    val priceAtEntry: Money,
    amountCharged: Money? = null,
    status: ParkingStatus = ParkingStatus.ENTERED
) {
    private var _spotId: Long? = spotId
    val spotId: Long? get() = _spotId

    private var _parkedTime: Instant? = parkedTime
    val parkedTime: Instant? get() = _parkedTime

    private var _exitTime: Instant? = exitTime
    val exitTime: Instant? get() = _exitTime

    private var _amountCharged: Money? = amountCharged
    val amountCharged: Money? get() = _amountCharged

    private var _status: ParkingStatus = status
    val status: ParkingStatus get() = _status

    companion object {
        fun enter(
            licensePlate: LicensePlate,
            sectorName: SectorName,
            priceAtEntry: Money,
            entryTime: Instant
        ): ParkingSession = ParkingSession(
            licensePlate = licensePlate,
            sectorName = sectorName,
            entryTime = entryTime,
            priceAtEntry = priceAtEntry
        )

        fun reconstitute(
            id: Long,
            licensePlate: LicensePlate,
            sectorName: SectorName,
            spotId: Long?,
            entryTime: Instant,
            parkedTime: Instant?,
            exitTime: Instant?,
            priceAtEntry: Money,
            amountCharged: Money?,
            status: ParkingStatus
        ): ParkingSession = ParkingSession(
            id = id,
            licensePlate = licensePlate,
            sectorName = sectorName,
            spotId = spotId,
            entryTime = entryTime,
            parkedTime = parkedTime,
            exitTime = exitTime,
            priceAtEntry = priceAtEntry,
            amountCharged = amountCharged,
            status = status
        )
    }

    fun park(spotId: Long, parkedTime: Instant) {
        check(_status == ParkingStatus.ENTERED) { "Can only park a vehicle with status ENTERED, current: $_status" }
        _spotId = spotId
        _parkedTime = parkedTime
        _status = ParkingStatus.PARKED
    }

    fun exit(amountCharged: Money, exitTime: Instant) {
        check(_status == ParkingStatus.PARKED) { "Can only exit a vehicle with status PARKED, current: $_status" }
        _amountCharged = amountCharged
        _exitTime = exitTime
        _status = ParkingStatus.EXITED
    }
}
