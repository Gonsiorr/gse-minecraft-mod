package gse.minu.network;

import gse.minu.entity.F18Entity;
import gse.minu.workshop.SailingCraftingMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class GSENetworking {
    private GSENetworking() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(F18ControlPayload.TYPE, F18ControlPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(F18HelmPayload.TYPE, F18HelmPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(F18BoardPayload.TYPE, F18BoardPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SailingBoatNamePayload.TYPE, SailingBoatNamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WindSyncPayload.TYPE, WindSyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(F18HelmPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.level().getEntity(payload.entityId()) instanceof F18Entity f18
                    && player.distanceToSqr(f18) <= 81.0D) {
                f18.handleSkipperInteraction(player);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(F18BoardPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.level().getEntity(payload.entityId()) instanceof F18Entity f18
                    // The F18 is long; validate a larger center radius than a player-sized entity.
                    && player.distanceToSqr(f18) <= 81.0D) {
                f18.boardPassenger(player);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SailingBoatNamePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof SailingCraftingMenu menu
                    && menu.containerId == payload.containerId() && menu.stillValid(player)) {
                menu.applyBoatName(payload.name());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(F18ControlPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.getVehicle() instanceof F18Entity f18
                    && f18.getId() == payload.entityId()
                    && f18.getControllingPassenger() == player) {
                f18.applyControls(payload.controls(), payload.actions());
            }
        });
    }
}
