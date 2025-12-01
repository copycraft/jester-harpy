package net.sylviameows.jesterrole;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class JesterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientLoginConnectionEvents.INIT.register((handler, server) -> {
            // jester win always false unless told otherwise by server.
        });

        ClientLoginConnectionEvents.QUERY_START.register((ClientLoginNetworkHandler handler, MinecraftClient client) -> {
            ClientLoginNetworking.registerReceiver(Jester.CHANNEL, (_client, _handler, buf, callbacksConsumer) -> {

                PacketByteBuf out = PacketByteBufs.create();
                var mod = FabricLoader.getInstance().getModContainer(Jester.MOD_ID);
                if (mod.isPresent()) {
                    String version = mod.get().getMetadata().getVersion().getFriendlyString();
                    return CompletableFuture.completedFuture(out.writeString(version));
                }

                return CompletableFuture.completedFuture(null);
            });
        });
    }
}
