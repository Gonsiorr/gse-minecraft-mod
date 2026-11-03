package gse.minu.network;

import gse.minu.GonsioRRSailingEngine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record F18ControlPayload(int entityId, int controls, int actions) implements CustomPacketPayload {
    public static final Type<F18ControlPayload> TYPE = new Type<>(GonsioRRSailingEngine.id("f18_controls"));
    public static final StreamCodec<RegistryFriendlyByteBuf, F18ControlPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, F18ControlPayload::entityId,
            ByteBufCodecs.VAR_INT, F18ControlPayload::controls,
            ByteBufCodecs.VAR_INT, F18ControlPayload::actions,
            F18ControlPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
