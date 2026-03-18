package com.marley.parking.domain.exception

class SectorFullException(message: String = "All sectors are full") : RuntimeException(message)
