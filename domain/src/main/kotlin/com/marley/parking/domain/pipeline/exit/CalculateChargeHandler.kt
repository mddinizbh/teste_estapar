package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.service.PricingService

class CalculateChargeHandler(
    private val pricingService: PricingService
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        val charge = pricingService.calculateCharge(session.priceAtEntry, session.entryTime, context.exitTime)

        return next(context.copy(amountCharged = charge))
    }
}
