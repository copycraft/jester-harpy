package net.sylviameows.jesterrole.mixin;

import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.sylviameows.jesterrole.Jester;
import net.sylviameows.jesterrole.cca.JesterWorldComponent;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(GameFunctions.class)
public abstract class MGameFunctions implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    @Unique
    private static final Random RANDOM = new Random();

    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void initializeGame(ServerWorld world, CallbackInfo ci) {
        JesterWorldComponent component = JesterWorldComponent.KEY.get(world);

        component.reset();
    }

    @Inject(
            method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "HEAD")
    )
    private static void jesterDeath(PlayerEntity victim, boolean spawnBody, @Nullable PlayerEntity killer, Identifier identifier, CallbackInfo ci) {
        if (killer == null) return;

        World world = victim.getWorld();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (world instanceof ServerWorld serverWorld) {
            // only run on server
            if (game.isRole(victim, Jester.ROLE) && game.isInnocent(killer)) {

                game.setLooseEndWinner(victim.getUuid());
                GameRoundEndComponent.KEY.get(world).setRoundEndData(serverWorld.getPlayers(), GameFunctions.WinStatus.LOOSE_END);

                JesterWorldComponent component = JesterWorldComponent.KEY.get(world);
                component.setJesterWin(true);

                GameFunctions.stopGame(serverWorld);
            }
        }

    }

}
