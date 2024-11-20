package com.zyte.generator.vertx

import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("VertxApp")

fun main() {
    runCatching {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(AppVerticle()).await()
    }.onFailure { ex ->
        logger.error("Could not start application", ex)
        exitProcess(-1)
    }
}