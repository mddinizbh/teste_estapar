package com.marley.parking.domain.exception

class DuplicateEntryException(message: String = "Vehicle already has an active session") : RuntimeException(message)
