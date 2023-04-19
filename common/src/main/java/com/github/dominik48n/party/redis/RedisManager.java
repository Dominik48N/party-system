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

import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.config.RedisConfig;
import com.github.dominik48n.party.user.UserManager;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {

    private final @NotNull List<RedisSubscription> subscriptions = Lists.newArrayList();
    private final @NotNull JedisPool jedisPool;
    private final @NotNull Consumer<Runnable> asyncConsumer;

    /**
     * Constructs a new RedisManager using the specified {@link RedisConfig}.
     *
     * @param config The {@link RedisConfig} to use.
     */
    public RedisManager(final @NotNull RedisConfig config, final @NotNull Consumer<Runnable> asyncConsumer) {
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
        this.asyncConsumer = asyncConsumer;
    }

    /**
     * Disconnects this RedisManager from the Redis server.
     */
    public void close() {
        this.subscriptions.forEach(RedisSubscription::close);
        this.jedisPool.destroy();
    }

    /**
     * Publishes a document to a Redis channel.
     *
     * @param channel  The name of the channel to publish the document to.
     * @param document The {@link Document} to publish to the channel.
     */
    public void publish(final @NotNull String channel, final @NotNull Document document) {
        try (final Jedis jedis = this.jedisPool.getResource()) {
            jedis.publish(channel, document.toString());
        }
    }

    /**
     * Subscribes to the Redis pub/sub channel and handles incoming messages with a RedisMessageSub instance.
     * Uses the provided {@link UserManager} to handle user management.
     *
     * @param userManager The {@link UserManager} instance to use for user management.
     * @param <TUser>     The type of user managed by the UserManager.
     */
    public <TUser> void subscribes(final @NotNull UserManager<TUser> userManager) {
        this.subscriptions.clear();
        this.subscriptions.add(new RedisMessageSub<>(userManager));
        this.subscriptions.add(new RedisSwitchServerSub<>(userManager));

        try (final Jedis jedis = this.jedisPool().getResource()) {
            this.subscriptions.forEach(subscription -> this.asyncConsumer.accept(() -> jedis.subscribe(subscription, subscription.channels())));
        }
    }

    public @NotNull JedisPool jedisPool() {
        return this.jedisPool;
    }
}
