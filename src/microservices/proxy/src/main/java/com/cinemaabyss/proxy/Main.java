package com.cinemaabyss.proxy;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.port;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Читаем порт из переменной окружения, по умолчанию 8000
        final int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));

        final Config config = new Config();
        logger.info("Configuration loaded: monolith={}, movies={}, migration={}%",
                config.getMonolithUrl(),
                config.getMoviesServiceUrl(),
                config.getMoviesMigrationPercent());

        final ProxyService proxyService = new ProxyService();  // <-- создали сервис

        port(port);
        logger.info("Starting Proxy Service on port {}", port);

        get("/*", (req, res) -> {
            logger.info("Received request: {} {}", req.requestMethod(), req.pathInfo());
            try {
                return proxyService.handleRequest(config, req.pathInfo(), req.requestMethod());
            } catch (IOException | InterruptedException e) {
                logger.error("Error handling request: {}", e.getMessage(), e);
                res.status(500);
                res.type("application/problem+json");
                return ProblemJson.create(
                        500,
                        "Internal Server Error",
                        "Error proxying request: " + e.getMessage()
                );
            }
        });
        awaitInitialization();
        logger.info("Proxy Service started successfully!");
    }
}
