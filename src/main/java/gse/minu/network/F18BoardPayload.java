package gse.minu.network;

import gse.minu.GonsioRRSailingEngine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record F18BoardPayload(int entityId) implements CustomPacketPayload {
    public static final Type<F18BoardPayload> TYPE = new Type<>(GonsioRRSailingEngine.id("f18_board"));
    public static final StreamCodec<RegistryFriendlyByteBuf, F18BoardPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, F18BoardPayload::entityId, F18BoardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
