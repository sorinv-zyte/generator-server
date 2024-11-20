package com.zyte.generator.vertx

import com.zyte.generator.model.ApiResponse
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.*

private val logger = LoggerFactory.getLogger(AppVerticle::class.java)

@OptIn(ExperimentalCoroutinesApi::class)
private val dispatcher = Dispatchers.IO.limitedParallelism(32)

class AppVerticle : CoroutineVerticle() {

    private val payloadSizeBytes = System.getenv("PAYLOAD_SIZE")?.toIntOrNull() ?: (1024 * 10)
    private val delayMs = System.getenv("DELAY_MS")?.toLongOrNull()

    override suspend fun start() {

        val router = Router.router(vertx)
        router.post("/v1/extract").coroutineHandler { ctx -> generateContent(ctx) }

        val serverOptions = HttpServerOptions()
        serverOptions.setAcceptBacklog(0)

        vertx.createHttpServer(serverOptions)
            .requestHandler(router)
            .listen(config.getInteger("http.port", 8080))
            .coAwait()
    }

    override suspend fun stop() {
        logger.info("Stopping verticle")
    }

    private suspend fun generateContent(ctx: RoutingContext) {
        val apiResponse = generateResponse()
        ctx.response().putHeader("Content-Type", "application/json").end(
            JsonObject.mapFrom(apiResponse).encode()
        )
    }

    private suspend fun generateResponse(): ApiResponse {
        val payload = ByteArray(payloadSizeBytes).apply { Random().nextBytes(this) }
        val base64Payload = Base64.getEncoder().encodeToString(payload)

        delayMs?.let {
            logger.info("Delaying for $it")
            delay(it)
        }

        return ApiResponse(
            url = "https://example.com/",
            statusCode = 200,
            httpResponseBody = base64Payload
        )
    }
}

fun Route.coroutineHandler(block: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        CoroutineScope(dispatcher).launch {
            try {
                logger.info("Executing request")
                block(ctx)
            } catch (ex: Exception) {
                logger.error("Failed executing", ex)
                ctx.fail(ex)
            }
        }
    }
}