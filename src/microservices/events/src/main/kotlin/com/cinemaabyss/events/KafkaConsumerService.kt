package com.cinemaabyss.events

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import kotlin.concurrent.thread
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

object KafkaConsumerService {
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)
    private val objectMapper = jacksonObjectMapper()
    private var running = false

    private val consumer: KafkaConsumer<String, String> by lazy {
        val props =
                mapOf(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to Config.kafkaBrokers,
                        ConsumerConfig.GROUP_ID_CONFIG to "events-service-consumer-group",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to
                                StringDeserializer::class.java.name,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to
                                StringDeserializer::class.java.name,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "true"
                )

        logger.info("Initializing Kafka Consumer with brokers: ${Config.kafkaBrokers}")
        KafkaConsumer(props)
    }

    fun start() {
        if (running) {
            logger.warn("Consumer is already running")
            return
        }

        running = true

        thread(name = "kafka-consumer-thread", isDaemon = true) {
            logger.info("Starting Kafka Consumer thread")

            val topics = listOf("user-events", "movie-events", "payment-events")
            consumer.subscribe(topics)
            logger.info("Subscribed to topics: $topics")

            try {
                while (running) {
                    val records = consumer.poll(Duration.ofMillis(1000))

                    for (record in records) {
                        // Парсим JSON и достаём event_id
                        val jsonNode = objectMapper.readTree(record.value())
                        val eventId = jsonNode.get("id")?.asText()

                        if (eventId == null) {
                            logger.warn("Event without 'id' field: ${record.value()}")
                            continue
                        }

                        // Проверяем идемпотентность
                        if (PostgresService.isEventProcessed(eventId)) {
                            logger.info(
                                    "Event already processed, skipping | " +
                                            "event_id='$eventId', " +
                                            "topic='${record.topic()}', " +
                                            "partition=${record.partition()}, " +
                                            "offset=${record.offset()}"
                            )
                            continue
                        }

                        // Обрабатываем событие
                        logger.info(
                                "Received event from Kafka | " +
                                        "topic='${record.topic()}', " +
                                        "partition=${record.partition()}, " +
                                        "offset=${record.offset()}, " +
                                        "key='${record.key()}', " +
                                        "event_id='$eventId'"
                        )

                        // Помечаем как обработанное
                        PostgresService.markEventAsProcessed(eventId, record.topic())
                    }
                }
            } catch (e: Exception) {
                logger.error("Error in consumer loop: ${e.message}", e)
            } finally {
                consumer.close()
                logger.info("Kafka Consumer closed")
            }
        }
    }

    fun stop() {
        logger.info("Stopping Kafka Consumer")
        running = false
    }
}
