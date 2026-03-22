package com.marley.parking.integration

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@Factory
class TestClockFactory {

    @Singleton
    @Replaces(Clock::class)
    fun testClock(): Clock = Clock.fixed(
        Instant.parse("2026-01-01T10:02:00Z"),
        ZoneOffset.UTC
    )
}
