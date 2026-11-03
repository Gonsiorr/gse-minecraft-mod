package gse.minu.client;

import gse.minu.entity.F18Entity;
import gse.minu.network.F18ControlPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class F18InputHandler {
    private F18InputHandler() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !(client.player.getVehicle() instanceof F18Entity boat)) return;
            int controls=0;
            if (F18KeyBindings.LEFT.isDown()) controls |= 1;
            if (F18KeyBindings.RIGHT.isDown()) controls |= 2;
            if (F18KeyBindings.TRIM_IN.isDown()) controls |= 4;
            if (F18KeyBindings.TRIM_OUT.isDown()) controls |= 8;
            int actions=0;
            if (F18KeyBindings.SAILS.consumeClick()) actions |= 1;
            if (F18KeyBindings.GENNAKER.consumeClick()) actions |= 2;
            if (F18KeyBindings.ANCHOR.consumeClick()) actions |= 4;
            if (ClientPlayNetworking.canSend(F18ControlPayload.TYPE)) {
                ClientPlayNetworking.send(new F18ControlPayload(boat.getId(), controls, actions));
            }
        });
    }
}
