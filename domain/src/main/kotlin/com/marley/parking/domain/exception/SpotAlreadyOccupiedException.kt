package com.marley.parking.domain.exception

class SpotAlreadyOccupiedException(message: String = "Spot is already occupied") : RuntimeException(message)
