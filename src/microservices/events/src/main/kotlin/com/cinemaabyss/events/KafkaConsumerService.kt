package com.cinemaabyss.events

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.concurrent.thread

object KafkaConsumerService {
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)
    private var running = false
    
    private val consumer: KafkaConsumer<String, String> by lazy {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to Config.kafkaBrokers,
            ConsumerConfig.GROUP_ID_CONFIG to "events-service-consumer-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest", // Читаем с начала
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
        
        // Запускаем консьюмер в отдельном потоке
        thread(name = "kafka-consumer-thread", isDaemon = true) {
            logger.info("Starting Kafka Consumer thread")
            
            // Подписываемся на все три топика
            val topics = listOf("user-events", "movie-events", "payment-events")
            consumer.subscribe(topics)
            logger.info("Subscribed to topics: $topics")
            
            try {
                while (running) {
                    val records = consumer.poll(Duration.ofMillis(1000))
                    
                    for (record in records) {
                        logger.info(
                            "Received event from Kafka | " +
                            "topic='${record.topic()}', " +
                            "partition=${record.partition()}, " +
                            "offset=${record.offset()}, " +
                            "key='${record.key()}', " +
                            "value=${record.value()}"
                        )
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