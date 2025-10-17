package com.cinemaabyss.proxy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public String proxyRequest(String targetUrl, String path, String method) {
        try {
            final String fullUrl = targetUrl + path;
            logger.info("Proxying {} {} to {}", method, path, targetUrl);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .method(method, HttpRequest.BodyPublishers.noBody())
                    .build();

            final HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            logger.info("Response from {}: status={}", targetUrl, response.statusCode());
            return response.body();

        } catch (Exception e) {
            logger.error("Error proxying request to {}: {}", targetUrl, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}
