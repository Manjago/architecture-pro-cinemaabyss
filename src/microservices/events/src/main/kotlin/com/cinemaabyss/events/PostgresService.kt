package com.cinemaabyss.events

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory

object PostgresService {
    private val logger = LoggerFactory.getLogger(PostgresService::class.java)

    private val dataSource: HikariDataSource by lazy {
        val config =
                HikariConfig().apply {
                    jdbcUrl =
                            "jdbc:postgresql://${Config.postgresHost}:${Config.postgresPort}/${Config.postgresDb}"
                    username = Config.postgresUser
                    password = Config.postgresPassword

                    // Настройки пула для MVP с низкой нагрузкой
                    maximumPoolSize = 5 // Не нужно много соединений для 1 consumer thread
                    minimumIdle = 2 // Пара соединений всегда готова
                    connectionTimeout = 10000 // 10 сек - быстрее фейлиться при проблемах с БД
                    idleTimeout = 300000 // 5 минут - разумно для периодической активности
                    maxLifetime = 1800000 // 30 минут (дефолт, норм)
                }

        logger.info("Initializing HikariCP connection pool to PostgreSQL at ${config.jdbcUrl}")
        HikariDataSource(config)
    }

    fun initialize() {
        logger.info("Initializing database schema...")

        dataSource.connection.use { conn ->
            conn.createStatement()
                    .execute(
                            """
                CREATE TABLE IF NOT EXISTS processed_events (
                    event_id VARCHAR(255) PRIMARY KEY,
                    topic VARCHAR(100) NOT NULL,
                    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
                )
            """
                    )
        }

        logger.info("Database schema initialized successfully")
    }

    fun isEventProcessed(eventId: String): Boolean {
        dataSource.connection.use { conn ->
            val stmt =
                    conn.prepareStatement(
                            "SELECT EXISTS(SELECT 1 FROM processed_events WHERE event_id = ?)"
                    )
            stmt.setString(1, eventId)

            val rs = stmt.executeQuery()
            rs.next()
            return rs.getBoolean(1)
        }
    }

    fun markEventAsProcessed(eventId: String, topic: String): Boolean {
        return try {
            dataSource.connection.use { conn ->
                val stmt =
                        conn.prepareStatement(
                                "INSERT INTO processed_events (event_id, topic) VALUES (?, ?)"
                        )
                stmt.setString(1, eventId)
                stmt.setString(2, topic)

                stmt.executeUpdate()
                logger.debug("Marked event as processed: $eventId")
                true
            }
        } catch (e: Exception) {
            // UNIQUE VIOLATION - событие уже было обработано
            logger.warn(
                    "Failed to mark event as processed (likely duplicate): $eventId - ${e.message}"
            )
            false
        }
    }

    fun close() {
        logger.info("Closing HikariCP connection pool")
        dataSource.close()
    }
}
