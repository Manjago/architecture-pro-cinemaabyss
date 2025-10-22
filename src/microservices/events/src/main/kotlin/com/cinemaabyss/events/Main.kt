package com.cinemaabyss.events

import io.javalin.Javalin
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
