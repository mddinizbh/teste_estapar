package com.marley.parking.domain.model

import com.marley.parking.domain.exception.SpotAlreadyOccupiedException
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.SectorName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SpotTest : BehaviorSpec({

    Given("um spot livre") {
        val spot = Spot(
            id = 1L,
            sectorName = SectorName("A"),
            coordinates = Coordinates(-23.5, -46.6)
        )

        When("occupy() é chamado") {
            spot.occupy()

            Then("o spot deve ficar ocupado") {
                spot.isOccupied shouldBe true
            }
        }
    }

    Given("um spot já ocupado") {
        val spot = Spot(
            id = 1L,
            sectorName = SectorName("A"),
            coordinates = Coordinates(-23.5, -46.6),
            occupied = true
        )

        When("occupy() é chamado novamente") {
            Then("deve lançar SpotAlreadyOccupiedException") {
                shouldThrow<SpotAlreadyOccupiedException> {
                    spot.occupy()
                }
            }
        }
    }

    Given("um spot ocupado para release") {
        val spot = Spot(
            id = 1L,
            sectorName = SectorName("A"),
            coordinates = Coordinates(-23.5, -46.6),
            occupied = true
        )

        When("release() é chamado") {
            spot.release()

            Then("o spot deve ficar livre") {
                spot.isOccupied shouldBe false
            }
        }
    }

    Given("um spot já livre") {
        val spot = Spot(
            id = 1L,
            sectorName = SectorName("A"),
            coordinates = Coordinates(-23.5, -46.6)
        )

        When("release() é chamado") {
            spot.release()

            Then("o spot deve continuar livre") {
                spot.isOccupied shouldBe false
            }
        }
    }
})
