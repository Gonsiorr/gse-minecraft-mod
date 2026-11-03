package gse.minu.network;

import gse.minu.GonsioRRSailingEngine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record F18HelmPayload(int entityId) implements CustomPacketPayload {
    public static final Type<F18HelmPayload> TYPE = new Type<>(GonsioRRSailingEngine.id("f18_helm"));
    public static final StreamCodec<RegistryFriendlyByteBuf, F18HelmPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, F18HelmPayload::entityId, F18HelmPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
