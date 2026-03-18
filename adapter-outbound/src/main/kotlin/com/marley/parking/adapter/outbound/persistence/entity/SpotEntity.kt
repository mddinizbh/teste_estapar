package com.marley.parking.adapter.outbound.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "spot")
open class SpotEntity(
    @Id
    open var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    open var sector: SectorEntity = SectorEntity(),

    @Column(name = "lat", nullable = false)
    open var lat: Double = 0.0,

    @Column(name = "lng", nullable = false)
    open var lng: Double = 0.0,

    @Column(name = "occupied", nullable = false)
    open var occupied: Boolean = false
)
