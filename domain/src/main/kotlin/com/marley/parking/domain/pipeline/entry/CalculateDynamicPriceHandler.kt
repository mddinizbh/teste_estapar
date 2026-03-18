package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.service.PricingService

class CalculateDynamicPriceHandler(
    private val pricingService: PricingService
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val sector = context.sector!!
        val occupancyRate = context.occupancyRate!!
        val price = pricingService.calculateEntryPrice(sector.basePrice, occupancyRate)

        return next(context.copy(priceAtEntry = price))
    }
}
