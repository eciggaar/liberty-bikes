/**
 *
 */
package org.libertybikes.game.metric;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMetrics {
    // Metric names
    private static final String CURRENT_ROUNDS = "current_num_of_rounds";
    private static final String TOTAL_ROUNDS = "total_num_of_rounds";
    private static final String CURRENT_PLAYERS = "current_num_of_players";
    private static final String TOTAL_PLAYERS = "total_num_of_players";
    private static final String TOTAL_MOBILE_PLAYERS = "total_num_of_mobile_players";
    private static final String GAME_ROUND_TIMER = "game_round_timer";
    private static final String CURRENT_PARTIES = "current_number_of_parties";
    private static final String CURRENT_QUEUED_PLAYERS = "current_num_of_players_in_queue";
    private static final String OPEN_WEBSOCKET_TIMER = "open_game_websocket_timer";

    // Atomic counters for gauges
    private static final AtomicInteger currentRounds = new AtomicInteger(0);
    private static final AtomicInteger currentPlayers = new AtomicInteger(0);
    private static final AtomicInteger currentParties = new AtomicInteger(0);
    private static final AtomicInteger currentQueuedPlayers = new AtomicInteger(0);

    private static MetricRegistry registry;
    private static boolean metricsRegistered = false;

    private static MetricRegistry getRegistry() {
        try {
            if (registry == null) {
                registry = CDI.current().select(MetricRegistry.class).get();
                registerMetrics();
                System.out.println("MetricRegistry configured");
            }
            return registry;
        } catch (IllegalStateException ise) {
            System.out.println("WARNING: Unable to locate CDIProvider");
            ise.printStackTrace();
        }
        return null;
    }

    private static void registerMetrics() {
        if (metricsRegistered || registry == null) {
            return;
        }

        // Register gauges - MicroProfile Metrics 5.0+ uses Supplier<T> where T extends Number
        registry.gauge(CURRENT_ROUNDS, () -> currentRounds.get());
        registry.gauge(CURRENT_PLAYERS, () -> currentPlayers.get());
        registry.gauge(CURRENT_PARTIES, () -> currentParties.get());
        registry.gauge(CURRENT_QUEUED_PLAYERS, () -> currentQueuedPlayers.get());

        metricsRegistered = true;
    }

    public static void incrementCurrentRounds() {
        if (registry != null || (getRegistry() != null)) {
            currentRounds.incrementAndGet();
            registry.counter(TOTAL_ROUNDS).inc();
        }
    }

    public static void decrementCurrentRounds() {
        if (registry != null || (getRegistry() != null)) {
            currentRounds.decrementAndGet();
        }
    }

    public static void incrementCurrentPlayers() {
        if (registry != null || (getRegistry() != null)) {
            currentPlayers.incrementAndGet();
            registry.counter(TOTAL_PLAYERS).inc();
        }
    }

    public static void decrementCurrentPlayers() {
        if (registry != null || (getRegistry() != null)) {
            currentPlayers.decrementAndGet();
        }
    }

    public static void incrementMobilePlayers() {
        if (registry != null || (getRegistry() != null)) {
            registry.counter(TOTAL_MOBILE_PLAYERS).inc();
        }
    }

    public static void incrementCurrentParties() {
        if (registry != null || (getRegistry() != null)) {
            currentParties.incrementAndGet();
        }
    }

    public static void decrementCurrentParties() {
        if (registry != null || (getRegistry() != null)) {
            currentParties.decrementAndGet();
        }
    }

    public static void incrementQueuedPlayers() {
        if (registry != null || (getRegistry() != null)) {
            currentQueuedPlayers.incrementAndGet();
        }
    }

    public static void decrementQueuedPlayers() {
        if (registry != null || (getRegistry() != null)) {
            currentQueuedPlayers.decrementAndGet();
        }
    }

    public static Timer.Context startGameRoundTimer() {
        if (registry != null || (getRegistry() != null)) {
            return registry.timer(GAME_ROUND_TIMER).time();
        }
        return null;
    }

    public static Timer.Context startWebsocketTimer() {
        if (registry != null || (getRegistry() != null)) {
            return registry.timer(OPEN_WEBSOCKET_TIMER).time();
        }
        return null;
    }
}
