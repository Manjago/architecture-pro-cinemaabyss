package com.cinemaabyss.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static spark.Spark.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        final int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));

        final Config config = new Config();
        logger.info("Configuration loaded: monolith={}, movies={}, migration={}%",
                config.getMonolithUrl(),
                config.getMoviesServiceUrl(),
                config.getMoviesMigrationPercent());

        final ProxyService proxyService = new ProxyService();

        port(port);
        logger.info("Starting Proxy Service on port {}", port);

        // Логируем все входящие запросы
        before("/*", (req, res) -> {
            logger.info("Received request: {} {}", req.requestMethod(), req.pathInfo());
        });

        // Глобальный обработчик исключений
        exception(Exception.class, (e, req, res) -> {
            logger.error("Error handling request {} {}: {}",
                    req.requestMethod(), req.pathInfo(), e.getMessage(), e);
            res.status(500);
            res.type("application/problem+json");
            res.body(ProblemJson.internalError("Error proxying request: " + e.getMessage()));
        });

        // Универсальный роут для всех методов и путей
        get("/*", (req, res) -> proxyService.handleRequest(config, req, res));
        post("/*", (req, res) -> proxyService.handleRequest(config, req, res));
        put("/*", (req, res) -> proxyService.handleRequest(config, req, res));
        delete("/*", (req, res) -> proxyService.handleRequest(config, req, res));
        patch("/*", (req, res) -> proxyService.handleRequest(config, req, res));
        options("/*", (req, res) -> proxyService.handleRequest(config, req, res));
        head("/*", (req, res) -> proxyService.handleRequest(config, req, res));

        awaitInitialization();
        logger.info("Proxy Service started successfully!");
    }
}
