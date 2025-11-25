package net.sylviameows.jesterrole.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sylviameows.jesterrole.Jester;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRoundEndComponent.class)
public class MGameRoundEndComponent {

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

}
