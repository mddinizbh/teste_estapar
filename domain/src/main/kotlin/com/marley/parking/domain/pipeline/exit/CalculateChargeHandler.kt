package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.service.PricingService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

class CalculateChargeHandler(
    private val pricingService: PricingService
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        val durationMinutes = Duration.between(session.entryTime, context.exitTime).toMinutes()
        val charge = pricingService.calculateCharge(session.priceAtEntry, session.entryTime, context.exitTime)

        logger.info { "Cobrança calculada | plate=${context.licensePlate.value}, entryTime=${session.entryTime}, exitTime=${context.exitTime}, duraçãoMinutos=$durationMinutes, preçoPorHora=${session.priceAtEntry}, cobrança=$charge" }

        return next(context.copy(amountCharged = charge))
    }
}
