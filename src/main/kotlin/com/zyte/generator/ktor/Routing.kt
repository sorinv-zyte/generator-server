package com.zyte.generator.ktor

import com.zyte.generator.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import java.util.*

fun Application.configureRouting() {
    routing {
        extractRoute()
        metricsRoute()
    }
}

fun Route.metricsRoute() {
    route("metrics") {
        get() {
            call.respondText(Metrics.registry.scrape())
        }
    }
}


fun Route.extractRoute() {
    route("/v1/extract") {
        post {
            val delayMillis = System.getenv("REQUEST_DELAY")?.toLongOrNull() ?: 0L
            val payloadSizeBytes = System.getenv("PAYLOAD_SIZE")?.toIntOrNull() ?: 1024

            // Introduce delay if specified
            if (delayMillis > 0) {
                delay(delayMillis)
            }

            // Generate payload and encode to Base64
            val payload = ByteArray(payloadSizeBytes).apply { Random().nextBytes(this) }
            val base64Payload = Base64.getEncoder().encodeToString(payload)

            val response = ApiResponse(
                url = "https://example.com/",
                statusCode = 200,
                httpResponseBody = base64Payload
            )
            call.respond(HttpStatusCode.OK, response)
        }
    }
}