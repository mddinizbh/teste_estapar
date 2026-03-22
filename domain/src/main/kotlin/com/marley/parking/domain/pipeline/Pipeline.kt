package com.marley.parking.domain.pipeline

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class Pipeline<T>(private val handlers: List<PipelineHandler<T>>) {

    fun execute(context: T): T {
        logger.info { "Pipeline iniciado | handlers=${handlers.size}, contexto=${context!!::class.simpleName}" }

        val chain = handlers.foldRight({ ctx: T -> ctx }) { handler, next ->
            { ctx ->
                val handlerName = handler::class.simpleName
                logger.debug { "Executing handler: $handlerName" }
                val result = handler.handle(ctx, next)
                logger.debug { "Handler $handlerName completed" }
                result
            }
        }

        val result = chain(context)
        logger.info { "Pipeline concluído | contexto=${context!!::class.simpleName}" }
        return result
    }
}
