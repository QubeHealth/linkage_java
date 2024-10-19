package com.linkage.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

    private static Jedis jedis;
    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    // Private constructor to prevent instantiation
    private RedisClient() {
        // pass
    }

    // Static method to initialize Redis connection
    public static void start() {
        if (jedis == null) {
            try {
                jedis = new Jedis(HOST, PORT);
                LOGGER.info("Redis connection started at {}:{}", HOST, PORT);
            } catch (Exception e) {
                LOGGER.error("Failed to start Redis connection", e);
                throw new RuntimeException("Failed to start Redis connection", e);
            }
        }
    }

    // Static method to close Redis connection
    public static void stop() {
        if (jedis != null) {
            try {
                jedis.close();
                jedis = null;
                LOGGER.info("Redis connection closed.");
            } catch (Exception e) {
                LOGGER.error("Failed to close Redis connection", e);
            }
        }
    }

    // Static method to set data in Redis
    public static void set(String key, String value) {
        if (jedis == null) {
            throw new IllegalStateException("Redis connection is not initialized. Call RedisClient.start() first.");
        }
        try {
            jedis.set(key, value);
        } catch (Exception e) {
            LOGGER.error("Failed to set key in Redis: {}", key, e);
        }
    }

    // Static method to get data from Redis
    public static String get(String key) {
        if (jedis == null) {
            throw new IllegalStateException("Redis connection is not initialized. Call RedisClient.start() first.");
        }
        try {
            return jedis.get(key);
        } catch (Exception e) {
            LOGGER.error("Failed to get key from Redis: {}", key, e);
            return null;
        }
    }
}