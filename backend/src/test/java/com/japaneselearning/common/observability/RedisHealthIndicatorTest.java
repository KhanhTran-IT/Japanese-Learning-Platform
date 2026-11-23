package com.japaneselearning.common.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisHealthIndicator Resource Management & Readiness Tests")
class RedisHealthIndicatorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection connection;

    @InjectMocks
    private RedisHealthIndicator healthIndicator;

    @Test
    @DisplayName("Success Path: should report UP and properly close RedisConnection when PING returns PONG")
    void health_Success_ReturnsUpAndReleasesConnection() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus());
        assertEquals("active", health.getDetails().get("connection"));

        // Verify connection lifecycle: properly closed on success path
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Unexpected Response: should report DOWN and properly close RedisConnection when PING is unexpected")
    void health_UnexpectedPingResponse_ReturnsDownAndReleasesConnection() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("QUEUED");

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Unexpected PING response: QUEUED", health.getDetails().get("error"));

        // Verify connection lifecycle: properly closed on unexpected response path
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Null Response: should report DOWN and properly close RedisConnection when PING returns null")
    void health_NullPingResponse_ReturnsDownAndReleasesConnection() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn(null);

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Unexpected PING response: null", health.getDetails().get("error"));

        // Verify connection lifecycle: properly closed when ping returns null
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Exception Path: should report DOWN and properly close RedisConnection when PING throws exception")
    void health_PingThrowsException_ReturnsDownAndReleasesConnection() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenThrow(new RedisConnectionFailureException("Connection reset by peer"));

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("RedisConnectionFailureException: Connection reset by peer"));

        // Verify connection lifecycle: properly closed on exception path
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Close Throws Exception: should handle connection close failure gracefully and report DOWN")
    void health_CloseThrowsException_ReturnsDown() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");
        doThrow(new RuntimeException("Socket error during connection close")).when(connection).close();

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("Socket error during connection close"));

        // Verify close was attempted
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Factory Exception: should report DOWN when factory.getConnection() throws exception")
    void health_FactoryThrowsException_ReturnsDown() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenThrow(new RedisConnectionFailureException("Pool exhausted"));

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("RedisConnectionFailureException: Pool exhausted"));

        // Connection was never acquired, so no close call on mock
        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("Null Factory: should report DOWN when ConnectionFactory is null")
    void health_NullConnectionFactory_ReturnsDown() {
        when(redisTemplate.getConnectionFactory()).thenReturn(null);

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("RedisConnectionFactory is null", health.getDetails().get("error"));

        verify(connectionFactory, never()).getConnection();
        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("Null Connection: should report DOWN when factory.getConnection() returns null")
    void health_NullConnectionReturned_ReturnsDown() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(null);

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("RedisConnection is null", health.getDetails().get("error"));

        verifyNoInteractions(connection);
    }
}
