package com.cinemaabyss.events

import io.javalin.Javalin
import java.time.Instant
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("EventsService")

fun main() {
    logger.info("Starting Events Service on port ${Config.port}")

    // Проверяем подключение к Kafka
    checkKafkaConnection()

    val app = Javalin.create { config -> config.showJavalinBanner = false }.start(Config.port)

    // Health check endpoint
    app.get("/api/events/health") { ctx -> ctx.json(mapOf("status" to true)) }

    // User Event endpoint
    app.post("/api/events/user") { ctx ->
        val userEvent = ctx.bodyAsClass(UserEvent::class.java)

        val eventId = "user-${userEvent.userId}-${userEvent.action}-${System.currentTimeMillis()}"
        val timestamp = Instant.now().toString()

        val metadata = KafkaProducerService.sendEvent("user-events", userEvent)

        val event = Event(id = eventId, type = "user", timestamp = timestamp, payload = userEvent)

        val response =
                EventResponse(
                        status = "success",
                        partition = metadata.partition(),
                        offset = metadata.offset(),
                        event = event
                )

        ctx.status(201).json(response)
    }

    // Movie Event endpoint
    app.post("/api/events/movie") { ctx ->
        val movieEvent = ctx.bodyAsClass(MovieEvent::class.java)

        val eventId =
                "movie-${movieEvent.movieId}-${movieEvent.action}-${System.currentTimeMillis()}"
        val timestamp = Instant.now().toString()

        val metadata = KafkaProducerService.sendEvent("movie-events", movieEvent)

        val event = Event(id = eventId, type = "movie", timestamp = timestamp, payload = movieEvent)

        val response =
                EventResponse(
                        status = "success",
                        partition = metadata.partition(),
                        offset = metadata.offset(),
                        event = event
                )

        ctx.status(201).json(response)
    }

    // Payment Event endpoint
    app.post("/api/events/payment") { ctx ->
        val paymentEvent = ctx.bodyAsClass(PaymentEvent::class.java)

        val eventId = "payment-${paymentEvent.paymentId}-${System.currentTimeMillis()}"
        val timestamp = Instant.now().toString()

        val metadata = KafkaProducerService.sendEvent("payment-events", paymentEvent)

        val event =
                Event(id = eventId, type = "payment", timestamp = timestamp, payload = paymentEvent)

        val response =
                EventResponse(
                        status = "success",
                        partition = metadata.partition(),
                        offset = metadata.offset(),
                        event = event
                )

        ctx.status(201).json(response)
    }

    logger.info("Events Service started successfully on http://localhost:${Config.port}")
    logger.info("Health check available at http://localhost:${Config.port}/api/events/health")
}

private fun checkKafkaConnection() {
    logger.info("Checking Kafka connection to ${Config.kafkaBrokers}...")

    val props =
            mapOf(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to Config.kafkaBrokers,
                    AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG to "5000"
            )

    try {
        AdminClient.create(props).use { admin ->
            val clusterInfo = admin.describeCluster()
            val clusterId = clusterInfo.clusterId().get()
            logger.info("Successfully connected to Kafka cluster: $clusterId")
        }
    } catch (e: Exception) {
        logger.error("Failed to connect to Kafka: ${e.message}")
        throw e
    }
}
