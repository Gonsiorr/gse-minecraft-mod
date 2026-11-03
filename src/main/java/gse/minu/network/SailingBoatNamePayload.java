package gse.minu.network;

import gse.minu.GonsioRRSailingEngine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sends the name entered in the GSE workshop to the server for the F18 item in that menu. */
public record SailingBoatNamePayload(int containerId, String name) implements CustomPacketPayload {
    public static final Type<SailingBoatNamePayload> TYPE =
            new Type<>(GonsioRRSailingEngine.id("sailing_boat_name"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SailingBoatNamePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SailingBoatNamePayload::containerId,
                    ByteBufCodecs.stringUtf8(32), SailingBoatNamePayload::name,
                    SailingBoatNamePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
