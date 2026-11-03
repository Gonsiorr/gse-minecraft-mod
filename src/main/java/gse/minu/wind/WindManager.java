package gse.minu.wind;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import gse.minu.network.WindSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/** One deterministic server-wide wind field, transitioned gradually as specified for GSE. */
public final class WindManager {
    public record Snapshot(double knots, double directionFromDegrees, int beaufort) {}
    private static final Map<MinecraftServer, WindManager> INSTANCES = new WeakHashMap<>();
    private final Random random;
    private double knots;
    private double direction;
    private double startKnots;
    private double startDirection;
    private double targetKnots;
    private double targetDirection;
    private int transitionTicks;
    private long lastScheduleKey = Long.MIN_VALUE;
    private boolean wasStorming;
    private int syncTicker;

    private WindManager(long seed) {
        random = new Random(seed ^ System.currentTimeMillis());
        direction = random.nextDouble() * 360.0D;
        knots = chooseKnots(normalBeaufort());
        targetKnots = knots;
        targetDirection = direction;
    }

    public static synchronized WindManager get(MinecraftServer server) {
        return INSTANCES.computeIfAbsent(server, s -> new WindManager(s.overworld().getSeed()));
    }

    public static void registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (level == level.getServer().overworld()) {
                get(level.getServer()).tick(level);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            WindManager manager = get(server);
            if (++manager.syncTicker % 5 != 0) return;
            Snapshot snapshot = manager.snapshot();
            WindSyncPayload payload = new WindSyncPayload((float) snapshot.knots(),
                    (float) snapshot.directionFromDegrees(), snapshot.beaufort());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (ServerPlayNetworking.canSend(player, WindSyncPayload.TYPE)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        });
    }

    private void tick(ServerLevel level) {
        boolean storming = level.isThundering();
        if (storming != wasStorming) {
            wasStorming = storming;
            int picked = storming ? stormBeaufort() : normalBeaufort();
            knots = chooseKnots(picked);
            targetKnots = knots;
            startKnots = knots;
            transitionTicks = 0;
        }

        long day = level.getDayTime() / 24000L;
        int tickOfDay = (int) Math.floorMod(level.getDayTime(), 24000L);
        int slot = tickOfDay / 6000;
        long scheduleKey = day * 4L + slot;
        if (scheduleKey != lastScheduleKey) {
            lastScheduleKey = scheduleKey;
            if (slot == 3) {
                beginTransition(chooseKnots(normalBeaufort()), random.nextDouble() * 360.0D);
            } else if (slot < 3 && day > 0) {
                int previousBft = knotsToBeaufort(targetKnots);
                int nextBft = Math.max(0, Math.min(8, previousBft + (random.nextBoolean() ? 1 : -1)));
                beginTransition(chooseKnots(nextBft), targetDirection);
            }
        }

        if (transitionTicks > 0) {
            double progress = (1200.0D - transitionTicks + 1.0D) / 1200.0D;
            knots = lerp(startKnots, targetKnots, progress);
            direction = lerpAngle(startDirection, targetDirection, progress);
            transitionTicks--;
        }
    }

    private void beginTransition(double newKnots, double newDirection) {
        startKnots = knots;
        startDirection = direction;
        targetKnots = newKnots;
        targetDirection = newDirection;
        transitionTicks = 1200;
    }

    public Snapshot snapshot() {
        return new Snapshot(knots, direction, knotsToBeaufort(knots));
    }

    private int normalBeaufort() {
        int roll = random.nextInt(100);
        int cumulative = 0;
        int[] weights = {2, 3, 5, 12, 23, 25, 20, 6, 4};
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return i;
        }
        return 5;
    }

    private int stormBeaufort() {
        int band = random.nextInt(100);
        int base = band < 20 ? 4 : band < 60 ? 7 : band < 90 ? 9 : 11;
        int width = base == 4 ? 3 : base == 7 || base == 9 ? 2 : 2;
        return base + random.nextInt(width);
    }

    private double chooseKnots(int bft) {
        return switch (bft) {
            case 0 -> random.nextDouble() * 0.9D;
            case 1 -> between(1, 3); case 2 -> between(4, 6); case 3 -> between(7, 10);
            case 4 -> between(11, 16); case 5 -> between(17, 21); case 6 -> between(22, 27);
            case 7 -> between(28, 33); case 8 -> between(34, 40); case 9 -> between(41, 47);
            case 10 -> between(48, 55); case 11 -> between(56, 63); default -> 64.0D - 12.0D * Math.log(Math.max(1.0E-12D, random.nextDouble()));
        };
    }

    private double between(int low, int high) { return low + random.nextDouble() * (high - low); }

    public static int knotsToBeaufort(double value) {
        if (value < 1) return 0; if (value < 4) return 1; if (value < 7) return 2; if (value < 11) return 3;
        if (value < 17) return 4; if (value < 22) return 5; if (value < 28) return 6; if (value < 34) return 7;
        if (value < 41) return 8; if (value < 48) return 9; if (value < 56) return 10; if (value < 64) return 11; return 12;
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double lerpAngle(double a, double b, double t) {
        double delta = ((b - a + 540.0D) % 360.0D) - 180.0D;
        return (a + delta * t + 360.0D) % 360.0D;
    }
}
