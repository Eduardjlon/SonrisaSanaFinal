package com.uspgdevteam.sonrisasana.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Carga variables desde .env (local) o variables de entorno (Azure).
 */
public class AzureEnv {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        return value != null ? value : defaultValue;
    }
}
