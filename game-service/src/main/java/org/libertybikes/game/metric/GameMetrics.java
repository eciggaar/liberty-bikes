/**
 *
 */
package org.libertybikes.game.metric;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class GameMetrics {
    // Metric names
    private static final String TOTAL_ROUNDS = "total_num_of_rounds";
    private static final String TOTAL_PLAYERS = "total_num_of_players";
    private static final String TOTAL_MOBILE_PLAYERS = "total_num_of_mobile_players";
    private static final String GAME_ROUND_TIMER = "game_round_timer";
    private static final String OPEN_WEBSOCKET_TIMER = "open_game_websocket_timer";

    // Atomic counters for gauges
    private static final AtomicInteger currentRounds = new AtomicInteger(0);
    private static final AtomicInteger currentPlayers = new AtomicInteger(0);
    private static final AtomicInteger currentParties = new AtomicInteger(0);
    private static final AtomicInteger currentQueuedPlayers = new AtomicInteger(0);

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    private MetricRegistry registry;

    @PostConstruct
    public void init() {
        // Register gauges programmatically - MicroProfile Metrics 5.x uses Supplier
        registry.gauge("current_num_of_players", currentPlayers::get);
        registry.gauge("current_num_of_rounds", currentRounds::get);
        registry.gauge("current_number_of_parties", currentParties::get);
        registry.gauge("current_num_of_players_in_queue", currentQueuedPlayers::get);
        
    }

    // Static methods for updating counters
    public static void incrementCurrentRounds() {
        currentRounds.incrementAndGet();
    }
    
    public static void incrementTotalRounds() {
        incrementApplicationCounter(TOTAL_ROUNDS);
    }

    public static void decrementCurrentRounds() {
        currentRounds.decrementAndGet();
    }

    public static void incrementCurrentPlayers() {
        currentPlayers.incrementAndGet();
        incrementApplicationCounter(TOTAL_PLAYERS);
    }

    public static void decrementCurrentPlayers() {
        currentPlayers.decrementAndGet();
    }

    public static void incrementMobilePlayers() {
        incrementApplicationCounter(TOTAL_MOBILE_PLAYERS);
    }

    public static void incrementCurrentParties() {
        currentParties.incrementAndGet();
    }

    public static void decrementCurrentParties() {
        currentParties.decrementAndGet();
    }

    public static void incrementQueuedPlayers() {
        currentQueuedPlayers.incrementAndGet();
    }

    public static void decrementQueuedPlayers() {
        currentQueuedPlayers.decrementAndGet();
    }

    private static void incrementApplicationCounter(String metricName) {
        try {
            MetricRegistry applicationRegistry = CDI.current()
                .select(MetricRegistry.class, applicationRegistryType())
                .get();
            applicationRegistry.counter(metricName).inc();
        } catch (Exception e) {
            // Ignore if CDI not available
        }
    }

    private static RegistryType applicationRegistryType() {
        return new RegistryType() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RegistryType.class;
            }

            @Override
            public MetricRegistry.Type type() {
                return MetricRegistry.Type.APPLICATION;
            }
        };
    }

    public static AutoCloseable startGameRoundTimer() {
        try {
            GameMetrics instance = jakarta.enterprise.inject.spi.CDI.current().select(GameMetrics.class).get();
            if (instance.registry != null) {
                Timer timer = instance.registry.timer(GAME_ROUND_TIMER);
                long startTime = System.nanoTime();
                return () -> timer.update(Duration.ofNanos(System.nanoTime() - startTime));
            }
        } catch (Exception e) {
            // Ignore if CDI not available
        }
        return () -> {};
    }

    public static AutoCloseable startWebsocketTimer() {
        try {
            GameMetrics instance = jakarta.enterprise.inject.spi.CDI.current().select(GameMetrics.class).get();
            if (instance.registry != null) {
                Timer timer = instance.registry.timer(OPEN_WEBSOCKET_TIMER);
                long startTime = System.nanoTime();
                return () -> timer.update(Duration.ofNanos(System.nanoTime() - startTime));
            }
        } catch (Exception e) {
            // Ignore if CDI not available
        }
        return () -> {};
    }
}

// Made with Bob
