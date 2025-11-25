package net.sylviameows.jesterrole.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.sylviameows.jesterrole.Jester;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerMoodComponent.class)
public abstract class MPlayerMoodComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    @Final
    @Shadow
    private PlayerEntity player;

    @Shadow
    private float mood;

    @Shadow
    public abstract void sync();

    @Inject(
            method = "getMood",
            at = @At("RETURN"),
            cancellable = true
    )
    private void jester$getMood(CallbackInfoReturnable<Float> cir, @Local(name = "gameWorldComponent") GameWorldComponent game) {
        if (game.isRole(player, Jester.ROLE)) cir.setReturnValue(1f);
    }

    @Redirect(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/PlayerMoodComponent;setMood(F)V", ordinal = 0)
    )
    private void jester$serverTickMood(PlayerMoodComponent instance, float mood, @Local(name = "gameWorldComponent") GameWorldComponent game) {
        instance.setMood(jester$updateMood(mood, game));
    }

    @Redirect(
            method = "clientTick",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/PlayerMoodComponent;setMood(F)V", ordinal = 0)
    )
    private void jester$clientTickMood(PlayerMoodComponent instance, float mood) {
        var game = GameWorldComponent.KEY.get(this.player.getWorld());
        instance.setMood(jester$updateMood(mood, game));
    }

    @Unique
    private float jester$updateMood(float mood, GameWorldComponent game) {
        if (game.isRole(player, Jester.ROLE)) {
            return this.mood;
        }
        return mood;
    }

}
