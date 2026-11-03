package gse.minu.client;

import gse.minu.network.WindSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class WindHudState {
    private static volatile float knots;
    private static volatile float directionFrom;
    private static volatile int beaufort;

    private WindHudState() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(WindSyncPayload.TYPE, (payload, context) -> {
            knots = payload.knots();
            directionFrom = payload.directionFromDegrees();
            beaufort = payload.beaufort();
        });
    }

    public static float knots() { return knots; }
    public static float directionFrom() { return directionFrom; }
    public static int beaufort() { return beaufort; }
}
