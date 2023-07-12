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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisClusterException;

public class RedisManager extends JedisPubSub implements AutoCloseable {

    private final @NotNull ExecutorService executor = Executors.newFixedThreadPool(1);

    private final @NotNull List<RedisSubscription> subscriptions = Lists.newArrayList();
    private final @NotNull UnifiedJedis jedis;

    /**
     * Constructs a new RedisManager using the specified {@link RedisConfig}.
     *
     * @param config The {@link RedisConfig} to use.
     */
    public RedisManager(final @NotNull RedisConfig config) {
        final JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .user(config.username())
                .password(config.password())
                .timeoutMillis(Protocol.DEFAULT_TIMEOUT)
                .build();

        UnifiedJedis jedis;
        try {
            jedis = new JedisCluster(new HashSet<>(config.hosts()), clientConfig);
        } catch (final JedisClusterException e) {
            final HostAndPort host = config.hosts().stream().findAny().orElseThrow(() -> new IllegalStateException("No Redis node was found in the config."));
            jedis = new JedisPooled(host, clientConfig);
        }
        this.jedis = jedis;
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
    @Override
    public void close() {
        super.unsubscribe();
        this.jedis.close();
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
        this.jedis.publish(channel, message);
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

        this.executor.execute(
                () -> RedisManager.this.jedis.subscribe(this, this.subscriptions.stream().map(RedisSubscription::channel).toArray(String[]::new))
        );
    }

    public @NotNull UnifiedJedis jedis() {
        return this.jedis;
    }
}
