package com.marley.parking.domain.exception

class VehicleAlreadyParkedException(message: String = "Vehicle is already parked") : RuntimeException(message)
