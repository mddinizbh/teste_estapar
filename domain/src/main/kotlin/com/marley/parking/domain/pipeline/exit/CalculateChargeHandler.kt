package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.service.PricingService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CalculateChargeHandler(
    private val pricingService: PricingService
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        val charge = pricingService.calculateCharge(session.priceAtEntry, session.entryTime, context.exitTime)

        logger.info { "Charge calculated | plate=${context.licensePlate.value}, pricePerHour=${session.priceAtEntry}, charge=$charge" }

        return next(context.copy(amountCharged = charge))
    }
}
