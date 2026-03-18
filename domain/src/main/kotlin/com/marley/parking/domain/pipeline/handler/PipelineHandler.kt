package com.marley.parking.domain.pipeline.handler

interface PipelineHandler<T> {
    fun handle(context: T, next: (T) -> T): T
}
