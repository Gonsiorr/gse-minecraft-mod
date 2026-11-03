package gse.minu.network;

import gse.minu.GonsioRRSailingEngine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WindSyncPayload(float knots, float directionFromDegrees, int beaufort) implements CustomPacketPayload {
    public static final Type<WindSyncPayload> TYPE = new Type<>(GonsioRRSailingEngine.id("wind_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WindSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, WindSyncPayload::knots,
            ByteBufCodecs.FLOAT, WindSyncPayload::directionFromDegrees,
            ByteBufCodecs.VAR_INT, WindSyncPayload::beaufort,
            WindSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
