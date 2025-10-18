package com.cinemaabyss.proxy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

public class ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);
    private final HttpClient httpClient;

    // Заголовки, которые не нужно проксировать (управляются HttpClient)
    private static final Set<String> SKIP_HEADERS = Set.of(
            "host", "connection", "content-length", "transfer-encoding"
    );

    public ProxyService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public String handleRequest(Config config, Request sparkRequest) throws IOException, InterruptedException {
        final String targetUrl = selectTarget(config, sparkRequest.pathInfo());
        return proxyRequest(targetUrl, sparkRequest);
    }

    private String selectTarget(Config config, String path) {
        if (!path.startsWith("/api/movies")) {
            logger.info("Path {} -> MONOLITH (not a movies path)", path);
            return config.getMonolithUrl();
        }

        if (!config.isGradualMigration()) {
            logger.info("Path {} -> MONOLITH (migration disabled)", path);
            return config.getMonolithUrl();
        }

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

    private String proxyRequest(String targetUrl, Request sparkRequest) throws IOException, InterruptedException {
        final String method = sparkRequest.requestMethod();
        final String path = sparkRequest.pathInfo();
        final String queryString = sparkRequest.queryString();

        // Формируем полный URL с query параметрами
        final String fullUrl = targetUrl + path + (queryString != null ? "?" + queryString : "");

        logger.info("Proxying {} {} to {}", method, path, fullUrl);

        // Определяем тело запроса
        final BodyPublisher bodyPublisher;
        final String body = sparkRequest.body();
        if (body != null && !body.isEmpty()) {
            bodyPublisher = BodyPublishers.ofString(body);
            logger.debug("Request body: {} bytes", body.length());
        } else {
            bodyPublisher = BodyPublishers.noBody();
        }

        // Строим запрос
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .method(method, bodyPublisher);

        // Копируем заголовки (кроме служебных)
        sparkRequest.headers().forEach(header -> {
            final String headerLower = header.toLowerCase();
            if (!SKIP_HEADERS.contains(headerLower)) {
                final String value = sparkRequest.headers(header);
                requestBuilder.header(header, value);
                logger.debug("Copying header: {}: {}", header, value);
            }
        });

        final HttpRequest request = requestBuilder.build();
        final HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        logger.info("Response status: {}", response.statusCode());
        return response.body();
    }
}
