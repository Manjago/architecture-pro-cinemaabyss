package com.cinemaabyss.proxy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);
    private final HttpClient httpClient;

    public ProxyService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public String handleRequest(Config config, String path, String method) throws IOException, InterruptedException {
        final String targetUrl = selectTarget(config, path);
        return proxyRequest(targetUrl, path, method);
    }

    private String selectTarget(Config config, String path) {
        // Если путь не начинается с /api/movies - всегда монолит
        if (!path.startsWith("/api/movies")) {
            logger.info("Path {} -> MONOLITH (not a movies path)", path);
            return config.getMonolithUrl();
        }

        // Если миграция выключена - всегда монолит
        if (!config.isGradualMigration()) {
            logger.info("Path {} -> MONOLITH (migration disabled)", path);
            return config.getMonolithUrl();
        }

        // Генерируем случайное число 0-99
        final int randomValue = ThreadLocalRandom.current().nextInt(100);
        final int migrationPercent = config.getMoviesMigrationPercent();

        if (randomValue < migrationPercent) {
            logger.info("Path {} -> MOVIES SERVICE ({}% < {}%)", path, randomValue, migrationPercent);
            return config.getMoviesServiceUrl();
        } else {
            logger.info("Path {} -> MONOLITH ({}% >= {}%)", path, randomValue, migrationPercent);
            return config.getMonolithUrl();
        }
    }

    private String proxyRequest(String targetUrl, String path, String method) throws IOException, InterruptedException {
        final String fullUrl = targetUrl + path;
        logger.info("Proxying {} {} to {}", method, path, fullUrl);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();

        final HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        logger.info("Response status: {}", response.statusCode());
        return response.body();

    }
}
