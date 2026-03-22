package com.marley.parking.adapter.outbound.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "parking_session")
open class ParkingSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(name = "license_plate", nullable = false, length = 20)
    open var licensePlate: String = "",

    @Column(name = "sector_id", nullable = false)
    open var sectorId: Long = 0,

    @Column(name = "spot_id")
    open var spotId: Long? = null,

    @Column(name = "entry_time", nullable = false)
    open var entryTime: Instant = Instant.EPOCH,

    @Column(name = "parked_time")
    open var parkedTime: Instant? = null,

    @Column(name = "exit_time")
    open var exitTime: Instant? = null,

    @Column(name = "price_at_entry", nullable = false, precision = 10, scale = 2)
    open var priceAtEntry: BigDecimal = BigDecimal.ZERO,

    @Column(name = "amount_charged", precision = 10, scale = 2)
    open var amountCharged: BigDecimal? = null,

    @Column(name = "status", nullable = false, length = 10)
    open var status: String = "",

    @Version
    open var version: Int = 0
)
