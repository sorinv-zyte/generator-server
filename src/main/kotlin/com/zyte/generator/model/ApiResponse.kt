package com.zyte.generator.model

data class ApiResponse(
    val url: String,
    val statusCode: Int,
    val httpResponseBody: String
)