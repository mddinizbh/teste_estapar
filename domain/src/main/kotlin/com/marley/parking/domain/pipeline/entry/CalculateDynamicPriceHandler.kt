package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.service.PricingService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CalculateDynamicPriceHandler(
    private val pricingService: PricingService
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val sector = context.sector!!
        val occupancyRate = context.occupancyRate!!
        val price = pricingService.calculateEntryPrice(sector.basePrice, occupancyRate)

        logger.info { "Dynamic price calculated | sector=${sector.name.value}, base=${sector.basePrice}, entry=$price" }

        return next(context.copy(priceAtEntry = price))
    }
}
