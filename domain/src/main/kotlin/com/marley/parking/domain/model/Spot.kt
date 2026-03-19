package com.marley.parking.domain.model

import com.marley.parking.domain.exception.SpotAlreadyOccupiedException
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.SectorName

class Spot(
    val id: Long,
    val sectorName: SectorName,
    val coordinates: Coordinates,
    occupied: Boolean = false
) {
    private var _occupied: Boolean = occupied
    val isOccupied: Boolean get() = _occupied

    fun occupy() {
        if (_occupied) throw SpotAlreadyOccupiedException("Spot $id is already occupied")
        _occupied = true
    }

    fun release() {
        _occupied = false
    }
}
