/*
 * Copyright 2023 Dominik48N
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dominik48n.party.redis;

import com.github.dominik48n.party.config.RedisConfig;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {

    private final @NotNull JedisPool jedisPool;

    /**
     * Constructs a new RedisManager using the specified {@link RedisConfig}.
     *
     * @param config The {@link RedisConfig} to use.
     */
    public RedisManager(final @NotNull RedisConfig config) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(16);
        poolConfig.setMinIdle(8);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000L));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000L));
        this.jedisPool = new JedisPool(poolConfig, config.hostname(), config.port(), 3000, config.username(), config.password());
    }

    /**
     * Disconnects this RedisManager from the Redis server.
     */
    public void close() {
        this.jedisPool.destroy();
    }

    public @NotNull JedisPool jedisPool() {
        return this.jedisPool;
    }
}
