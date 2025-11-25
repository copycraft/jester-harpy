package net.sylviameows.jesterrole.mixin;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.item.LockpickItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.sylviameows.jesterrole.Jester;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LockpickItem.class)
public class MLockpickItem {

    @Redirect(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSneaking()Z")
    )
    private static boolean jester$useOnBlock(PlayerEntity player) {
        if (player.isSneaking()) {
            if (TMMClient.gameComponent.isRole(player, Jester.ROLE)) {
                player.sendMessage(Text.translatable("tip.lockpick.killer_only"), true);
                return false;
            }
            return true;
        }
        return false;
    }

}
