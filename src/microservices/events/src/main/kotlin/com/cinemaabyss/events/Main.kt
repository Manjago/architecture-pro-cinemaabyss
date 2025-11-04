package com.cinemaabyss.events

import io.javalin.Javalin
import io.javalin.http.Context
import java.time.Instant
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("EventsService")

fun main() {
    logger.info("Starting Events Service on port ${Config.port}")

    // Проверяем подключение к Kafka
    checkKafkaConnection()
    // Инициализируем PostgreSQL
    PostgresService.initialize()

    // Запускаем Kafka Consumer
    KafkaConsumerService.start()

    val app = Javalin.create { config -> config.showJavalinBanner = false }.start(Config.port)

    // Health check endpoint
    app.get("/api/events/health") { ctx -> ctx.json(mapOf("status" to true)) }

    // User Event endpoint
    app.post("/api/events/user") { ctx ->
        handleEvent<UserEvent>(ctx, "user-events", "user") { event ->
            "user-${event.userId}-${event.action}-${System.currentTimeMillis()}"
        }
    }

    // Movie Event endpoint
    app.post("/api/events/movie") { ctx ->
        handleEvent<MovieEvent>(ctx, "movie-events", "movie") { event ->
            "movie-${event.movieId}-${event.action}-${System.currentTimeMillis()}"
        }
    }

    // Payment Event endpoint
    app.post("/api/events/payment") { ctx ->
        handleEvent<PaymentEvent>(ctx, "payment-events", "payment") { event ->
            "payment-${event.paymentId}-${System.currentTimeMillis()}"
        }
    }

    logger.info("Events Service started successfully on http://localhost:${Config.port}")
    logger.info("Health check available at http://localhost:${Config.port}/api/events/health")

    // Graceful shutdown
    Runtime.getRuntime()
            .addShutdownHook(
                    Thread {
                        logger.info("Shutdown signal received, cleaning up...")
                        KafkaConsumerService.stop()
                        KafkaProducerService.close()
                        PostgresService.close()
                        app.stop()
                        logger.info("Service stopped gracefully")
                    }
            )
}

private inline fun <reified T : EventPayload> handleEvent(
        ctx: Context,
        topic: String,
        eventType: String,
        eventIdGenerator: (T) -> String
) {
    val payload = ctx.bodyAsClass(T::class.java)
    val eventId = eventIdGenerator(payload)
    val timestamp = Instant.now().toString()

    // Создаём обёрнутое событие
    val event = Event(id = eventId, type = eventType, timestamp = timestamp, payload = payload)

    // Отправляем в Kafka целиком Event, а не payload!
    val metadata = KafkaProducerService.sendEvent(topic, event)

    val response =
            EventResponse(
                    status = "success",
                    partition = metadata.partition(),
                    offset = metadata.offset(),
                    event = event
            )

    ctx.status(201).json(response)
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
