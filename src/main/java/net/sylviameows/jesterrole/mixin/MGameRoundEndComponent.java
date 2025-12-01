package net.sylviameows.jesterrole.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.sylviameows.jesterrole.Jester;
import net.sylviameows.jesterrole.cca.JesterWorldComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(GameRoundEndComponent.class)
public class MGameRoundEndComponent {

    @Shadow
    @Final
    private World world;

    @ModifyArg(
            method = "setRoundEndData",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/GameRoundEndComponent$RoundEndData;<init>(Lcom/mojang/authlib/GameProfile;Ldev/doctor4t/trainmurdermystery/client/gui/RoleAnnouncementTexts$RoleAnnouncementText;Z)V")
    )
    private static RoleAnnouncementTexts.RoleAnnouncementText jesterRole(RoleAnnouncementTexts.RoleAnnouncementText value, @Local(name = "game") GameWorldComponent game, @Local(name = "player") ServerPlayerEntity player) {
        if (game.isRole(player, Jester.ROLE)) {
            return Jester.TEXT;
        }
        return value;
    }

    @Inject(
            method = "didWin",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true
    )
    private void jester$didWin(UUID uuid, CallbackInfoReturnable<Boolean> cir, @Local(name = "detail") GameRoundEndComponent.RoundEndData detail) {
        if (detail.role().equals(Jester.TEXT)) {
            JesterWorldComponent component = JesterWorldComponent.KEY.get(world);

            if (component.isJesterWin()) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

}
