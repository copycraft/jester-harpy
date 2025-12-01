package net.sylviameows.jesterrole.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.sylviameows.jesterrole.Jester;
import net.sylviameows.jesterrole.cca.JesterWorldComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mixin(MurderGameMode.class)
public class MMurderGameMode {

    @Unique
    private static final Random RANDOM = new Random();

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
            @Local(name = "gameWorldComponent") GameWorldComponent game,
            @Local(name = "players") List<ServerPlayerEntity> players
    ) {
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

    @Inject(
            method = "assignRolesAndGetKillerCount",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/trainmurdermystery/cca/ScoreboardRoleSelectorComponent;assignVigilantes(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/trainmurdermystery/cca/GameWorldComponent;Ljava/util/List;I)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void assignJester(@NotNull ServerWorld world, @NotNull List<ServerPlayerEntity> players, GameWorldComponent gameComponent, CallbackInfoReturnable<Integer> cir) {
        JesterWorldComponent component = JesterWorldComponent.KEY.get(world);
        if (!component.isEnabled()) return;

        List<ServerPlayerEntity> potentials = new ArrayList<>(players.stream().filter(player -> gameComponent.isRole(player, TMMRoles.CIVILIAN)).toList());

        int count = 0;
        if (!Jester.forcedJesters.isEmpty()) {
            for (UUID uuid : Jester.forcedJesters) {
                if (!gameComponent.isRole(uuid, TMMRoles.CIVILIAN)) {
                    continue;
                }

                potentials.removeIf(player -> player.getUuid().equals(uuid));

                PlayerEntity player = world.getPlayerByUuid(uuid);
                if (player instanceof ServerPlayerEntity serverPlayer && players.contains(serverPlayer) && gameComponent.isRole(player, Jester.ROLE)) {
                    player.giveItemStack(new ItemStack(TMMItems.LOCKPICK));
                }

                gameComponent.addRole(uuid, Jester.ROLE);
                count++;
            }

            Jester.forcedJesters.clear();
        }

        if (count < 1) {
            int selection = RANDOM.nextInt(potentials.size());
            ServerPlayerEntity player = potentials.get(selection);

            player.giveItemStack(new ItemStack(TMMItems.LOCKPICK));
            gameComponent.addRole(player, Jester.ROLE);
        }
    }


}
