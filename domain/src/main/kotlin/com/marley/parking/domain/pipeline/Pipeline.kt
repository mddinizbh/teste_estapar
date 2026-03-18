package com.marley.parking.domain.pipeline

import com.marley.parking.domain.pipeline.handler.PipelineHandler

class Pipeline<T>(private val handlers: List<PipelineHandler<T>>) {

    fun execute(context: T): T {
        val chain = handlers.foldRight({ ctx: T -> ctx }) { handler, next ->
            { ctx -> handler.handle(ctx, next) }
        }
        return chain(context)
    }
}
