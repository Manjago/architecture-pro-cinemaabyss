package com.cinemaabyss.proxy;

public class Config {
    private final String monolithUrl;
    private final String moviesServiceUrl;
    private final boolean gradualMigration;
    private final int moviesMigrationPercent;

    public Config() {
        this.monolithUrl = getEnv("MONOLITH_URL", "http://localhost:8080");
        this.moviesServiceUrl = getEnv("MOVIES_SERVICE_URL", "http://localhost:8081");
        this.gradualMigration = Boolean.parseBoolean(getEnv("GRADUAL_MIGRATION", "true"));
        this.moviesMigrationPercent = Integer.parseInt(getEnv("MOVIES_MIGRATION_PERCENT", "50"));
    }

    private String getEnv(String key, String defaultValue) {
        final String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public String getMonolithUrl() {
        return monolithUrl;
    }

    public String getMoviesServiceUrl() {
        return moviesServiceUrl;
    }

    public boolean isGradualMigration() {
        return gradualMigration;
    }

    public int getMoviesMigrationPercent() {
        return moviesMigrationPercent;
    }
}