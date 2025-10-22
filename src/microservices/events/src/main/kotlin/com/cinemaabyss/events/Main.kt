package com.cinemaabyss.events

import io.javalin.Javalin
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("EventsService")

fun main() {
    logger.info("Starting Events Service on port ${Config.port}")

    val app = Javalin.create { config -> config.showJavalinBanner = false }.start(Config.port)

    // Health check endpoint
    app.get("/api/events/health") { ctx -> ctx.json(mapOf("status" to true)) }

    logger.info("Events Service started successfully on http://localhost:${Config.port}")
    logger.info("Health check available at http://localhost:${Config.port}/api/events/health")
}
