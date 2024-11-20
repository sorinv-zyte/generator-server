package com.zyte.generator.ktor

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

object Metrics {

    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}