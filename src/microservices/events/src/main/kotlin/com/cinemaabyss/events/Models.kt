package com.cinemaabyss.events

import com.fasterxml.jackson.annotation.JsonProperty

// ========== Input Events (что приходит от клиента) ==========

interface EventPayload

data class UserEvent(
        @JsonProperty("user_id") val userId: Int,
        val username: String? = null,
        val email: String? = null,
        val action: String,
        val timestamp: String
) : EventPayload

data class MovieEvent(
        @JsonProperty("movie_id") val movieId: Int,
        val title: String,
        val action: String,
        @JsonProperty("user_id") val userId: Int? = null,
        val rating: Float? = null,
        val genres: List<String>? = null,
        val description: String? = null
) : EventPayload

data class PaymentEvent(
        @JsonProperty("payment_id") val paymentId: Int,
        @JsonProperty("user_id") val userId: Int,
        val amount: Float,
        val status: String,
        val timestamp: String,
        @JsonProperty("method_type") val methodType: String? = null
) : EventPayload

// ========== Event wrapper (что возвращаем в ответе) ==========

data class Event(
        val id: String, // "user-123-registered-1729604385123"
        val type: String, // "user", "movie", "payment"
        val timestamp: String, // ISO8601 время обработки
        val payload: EventPayload // UserEvent | MovieEvent | PaymentEvent
)

// ========== API Response ==========

data class EventResponse(
        val status: String, // "success"
        val partition: Int, // Kafka partition
        val offset: Long, // Kafka offset
        val event: Event // Обёрнутое событие
)
