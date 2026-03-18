package com.marley.parking.adapter.outbound.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "sector")
open class SectorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(name = "name", unique = true, nullable = false, length = 10)
    open var name: String = "",

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    open var basePrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "max_capacity", nullable = false)
    open var maxCapacity: Int = 0
)
