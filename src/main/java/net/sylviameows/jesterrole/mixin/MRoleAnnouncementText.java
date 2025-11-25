package net.sylviameows.jesterrole.mixin;

import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.text.Text;
import net.sylviameows.jesterrole.Jester;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RoleAnnouncementTexts.RoleAnnouncementText.class)
public class MRoleAnnouncementText {
    @Inject(
            method = "getEndText",
            at = @At("HEAD"),
            cancellable = true
    )
    public void jesterWin(GameFunctions.WinStatus status, Text winner, CallbackInfoReturnable<Text> cir) {
        if (Jester.isJesterWin()) {
            Text text = Text.translatable("announcement.win.jester", winner).withColor(Jester.ROLE_COLOR);
            cir.setReturnValue(text);
        }
    }
}
