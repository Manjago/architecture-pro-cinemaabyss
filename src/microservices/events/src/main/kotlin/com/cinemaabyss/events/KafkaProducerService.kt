package com.cinemaabyss.events

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

object KafkaProducerService {
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)
    private val objectMapper = jacksonObjectMapper()

    private val producer: KafkaProducer<String, String> by lazy {
        val props =
                mapOf(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to Config.kafkaBrokers,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to
                                StringSerializer::class.java.name,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to
                                StringSerializer::class.java.name,
                        ProducerConfig.ACKS_CONFIG to "all",
                        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true"
                )

        logger.info("Initializing Kafka Producer with brokers: ${Config.kafkaBrokers}")
        KafkaProducer(props)
    }

    fun sendEvent(topic: String, payload: EventPayload): RecordMetadata {
        val key = generateKey(payload)
        val value = objectMapper.writeValueAsString(payload)

        val record = ProducerRecord(topic, key, value)
        val metadata = producer.send(record).get() // .get() блокирует поток, но для MVP проще

        logger.info(
                "Sent event to topic '$topic' with key '$key', partition=${metadata.partition()}, offset=${metadata.offset()}"
        )
        return metadata
    }

    private fun generateKey(payload: EventPayload): String {
        return when (payload) {
            is UserEvent -> "user-${payload.userId}"
            is MovieEvent -> "movie-${payload.movieId}"
            is PaymentEvent -> "payment-${payload.paymentId}"
            else ->
                    throw IllegalArgumentException(
                            "Unknown payload type: ${payload::class.simpleName}"
                    )
        }
    }

    fun close() {
        logger.info("Closing Kafka Producer")
        producer.close()
    }
}
