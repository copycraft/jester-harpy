package net.sylviameows.jesterrole;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;

public record JesterPayload(boolean isJesterWin) implements CustomPayload {
    public static final Id<JesterPayload> ID = new Id<>(Jester.id("jester"));
    public static final PacketCodec<PacketByteBuf, JesterPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, JesterPayload::isJesterWin, JesterPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<JesterPayload> {
        @Override
        public void receive(@NotNull JesterPayload payload, ClientPlayNetworking.@NotNull Context context) {
            Jester.setJesterWin(payload.isJesterWin, null);
        }
    }
}
