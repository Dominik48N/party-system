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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisManager extends JedisPubSub {

    private final @NotNull ExecutorService executor = Executors.newFixedThreadPool(1);

    private final @NotNull List<RedisSubscription> subscriptions = Lists.newArrayList();
    private final @NotNull JedisPool jedisPool;

    /**
     * Constructs a new RedisManager using the specified {@link RedisConfig}.
     *
     * @param config The {@link RedisConfig} to use.
     */
    public RedisManager(final @NotNull RedisConfig config) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = config.username().isEmpty() ?
                new JedisPool(poolConfig, config.hostname(), config.port(), 3000, config.password()) :
                new JedisPool(poolConfig, config.hostname(), config.port(), 3000, config.username(), config.password());
    }

    @Override
    public void onMessage(final String channel, final String message) {
        this.subscriptions.forEach(subscription -> {
            if (subscription.channel().equals(channel)) subscription.onMessage(message);
        });
    }

    /**
     * Disconnects this RedisManager from the Redis server.
     */
    public void close() {
        super.unsubscribe();
        this.jedisPool.destroy();
    }

    /**
     * Publishes a document to a Redis channel.
     *
     * @param channel  The name of the channel to publish the document to.
     * @param document The {@link Document} to publish to the channel.
     *                 
     * @see #publish(String, String) 
     */
    public void publish(final @NotNull String channel, final @NotNull Document document) {
        this.publish(channel, document.toString());
    }

    /**
     * Publishes a string to a Redis channel.
     *
     * @param channel  The name of the channel to publish the document to.
     * @param message The message to publish to the channel.
     */
    public void publish(final @NotNull String channel, final @NotNull String message) {
        try (final Jedis jedis = this.jedisPool.getResource()) {
            jedis.publish(channel, message);
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
        this.subscriptions.add(new RedisUpdateUserPartySub<>(userManager));

        this.executor.execute(() -> {
            try (final Jedis jedis = RedisManager.this.jedisPool.getResource()) {
                jedis.subscribe(
                        this,
                        this.subscriptions.stream().map(RedisSubscription::channel).toArray(String[]::new)
                );
            }
        });
    }

    public @NotNull JedisPool jedisPool() {
        return this.jedisPool;
    }
}
