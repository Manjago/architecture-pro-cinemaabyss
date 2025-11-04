package com.cinemaabyss.events

object Config {
    val port: Int = getEnv("PORT", "8082").toInt()
    val kafkaBrokers: String = getEnv("KAFKA_BROKERS", "localhost:9092")
    val postgresHost: String = getEnv("POSTGRES_HOST", "localhost")
    val postgresPort: Int = getEnv("POSTGRES_PORT", "5432").toInt()
    val postgresDb: String = getEnv("POSTGRES_DB", "cinemaabyss")
    val postgresUser: String = getEnv("POSTGRES_USER", "postgres")
    val postgresPassword: String = getEnv("POSTGRES_PASSWORD", "postgres")

    private fun getEnv(key: String, defaultValue: String): String {
        return System.getenv(key) ?: defaultValue
    }
}
