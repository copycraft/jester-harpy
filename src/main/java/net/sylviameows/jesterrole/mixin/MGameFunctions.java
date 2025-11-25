package net.sylviameows.jesterrole.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.sylviameows.jesterrole.Jester;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(GameFunctions.class)
public abstract class MGameFunctions implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    @Unique
    private static final Random RANDOM = new Random();

    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void initializeGame(ServerWorld world, CallbackInfo ci) {
        Jester.setJesterWin(false, world.getPlayers());
    }

    @Redirect(
            method = "initializeGame",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking;send(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/packet/CustomPayload;)V"
            )
    )
    private static void jesterTitle(
            ServerPlayerEntity player,
            CustomPayload payload,
            @Local(name = "gameComponent") GameWorldComponent game,
            @Local(name = "players") List<ServerPlayerEntity> players
    ) {
        GameWorldComponent.GameMode gameMode = game.getGameMode();

        if (gameMode == GameWorldComponent.GameMode.LOOSE_ENDS) {
            ServerPlayNetworking.send(player, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.LOOSE_END), -1, -1));
        } else {
            int killerCount = game.getAllWithRole(TMMRoles.KILLER).size();

            ServerPlayNetworking.send(player, new AnnounceWelcomePayload(
                    RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(
                            game.isRole(player, TMMRoles.KILLER)
                                    ? RoleAnnouncementTexts.KILLER
                                    : game.isRole(player, TMMRoles.VIGILANTE)
                                    ? RoleAnnouncementTexts.VIGILANTE
                                    : game.isRole(player, Jester.ROLE)
                                    ? Jester.TEXT
                                    : RoleAnnouncementTexts.CIVILIAN),
                    killerCount, players.size() - killerCount)
            );
        }

    }


    @Inject(
            method = "assignRolesAndGetKillerCount",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/trainmurdermystery/cca/ScoreboardRoleSelectorComponent;assignVigilantes(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/trainmurdermystery/cca/GameWorldComponent;Ljava/util/List;I)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void assignJester(@NotNull ServerWorld world, @NotNull List<ServerPlayerEntity> players, GameWorldComponent gameComponent, CallbackInfoReturnable<Integer> cir) {
        if (!Jester.ENABLED) return;

        List<ServerPlayerEntity> potentials = players.stream().filter(player -> gameComponent.getRole(player) == null).toList();

        potentials.forEach(player -> Jester.LOGGER.info(player.getName().getString()));

        int selection = RANDOM.nextInt(potentials.size());

        ServerPlayerEntity player = potentials.get(selection);

        player.giveItemStack(new ItemStack(TMMItems.LOCKPICK));
        gameComponent.addRole(player, Jester.ROLE);
    }

    @Inject(
            method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "HEAD")
    )
    private static void jesterDeath(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier identifier, CallbackInfo ci) {
        if (killer == null) return;

        ServerWorld world = (ServerWorld) victim.getWorld();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (game.isRole(victim, Jester.ROLE) && game.isInnocent(killer)) {

            game.setLooseEndWinner(victim.getUuid());
            GameRoundEndComponent.KEY.get(world).setRoundEndData(world.getPlayers(), GameFunctions.WinStatus.LOOSE_END);

            Jester.setJesterWin(true, world.getPlayers());
            GameFunctions.stopGame(world);
        }
    }

}
