package net.sylviameows.jesterrole.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.sylviameows.jesterrole.Jester;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TMMClient.class)
public abstract class MTMMClient {

    @Shadow
    public static GameWorldComponent gameComponent;

    @Redirect(
            method = "getInstinctHighlight",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/GameWorldComponent;isInnocent(Lnet/minecraft/entity/player/PlayerEntity;)Z")
    )
    private static boolean isInnocent(GameWorldComponent instance, PlayerEntity player) {
        if (instance.isRole(player, Jester.ROLE)) return true;
        return instance.isInnocent(player);
    }

    @Inject(
            method = "getInstinctHighlight",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private static void getInstinctHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == -1) {
            if (isJester() && TMMClient.instinctKeybind.isPressed()) {
                if (target instanceof PlayerBodyEntity) cir.setReturnValue(0x606060);
            }
            return;
        };
        if (TMMClient.isPlayerSpectatingOrCreative() && target instanceof PlayerEntity player) {
            if (gameComponent.isRole(player, Jester.ROLE)) cir.setReturnValue(Jester.ROLE_COLOR);
        }
    }

    @Unique
    private static boolean isJester() {
        if (gameComponent == null) return false;
        assert MinecraftClient.getInstance().player != null;
        return gameComponent.isRole(MinecraftClient.getInstance().player, Jester.ROLE);
    }

}
