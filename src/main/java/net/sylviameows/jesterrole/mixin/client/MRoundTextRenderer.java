package net.sylviameows.jesterrole.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.client.gui.RoundTextRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.sylviameows.jesterrole.Jester;
import net.sylviameows.jesterrole.cca.JesterWorldComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoundTextRenderer.class)
public class MRoundTextRenderer {

    @ModifyVariable(
            method = "renderHud",
            at = @At(value = "STORE"),
            name = "winMessage"
    )
    private static MutableText renderHud(MutableText winMessage) {
        World world = MinecraftClient.getInstance().world;
        if (world != null) {
            JesterWorldComponent component = JesterWorldComponent.KEY.get(world);
            if (component.isJesterWin()) {
                return Text.translatable("game.win.jester");
            }
        }

        return winMessage;
    }

    @Unique
    @Final
    private static final ThreadLocal<Integer> CIVILIAN_TOTAL = new ThreadLocal<>();
    @Unique
    @Final
    private static final ThreadLocal<Integer> JESTER_COUNT = new ThreadLocal<>();

    @Inject(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/trainmurdermystery/cca/GameRoundEndComponent;getPlayers()Ljava/util/List;",
                    ordinal = 2
            )
    )
    private static void jester$renderTitle(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, CallbackInfo ci, @Local(name = "roundEnd") GameRoundEndComponent roundEnd) {
        int civilianTotal = 0;
        for (var entry : roundEnd.getPlayers())
            if (entry.role() == RoleAnnouncementTexts.CIVILIAN) civilianTotal += 1;
        CIVILIAN_TOTAL.set(civilianTotal);
        JESTER_COUNT.set(0);
        context.drawTextWithShadow(renderer, Jester.TEXT.titleText, -renderer.getWidth(Jester.TEXT.titleText) / 2 - 60, 14 + 16 + 24 * ((civilianTotal + 3) / 4), 0xFFFFFF);
    }

    @Inject(
            method = "renderHud",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/GameRoundEndComponent$RoundEndData;role()Ldev/doctor4t/trainmurdermystery/client/gui/RoleAnnouncementTexts$RoleAnnouncementText;", ordinal = 1)
    )
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private static void jester$renderPlayer(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, CallbackInfo ci, @Local(name = "entry") GameRoundEndComponent.RoundEndData entry) {
        if (entry.role() == Jester.TEXT) {
            int civilianTotal = CIVILIAN_TOTAL.get();
            int jesters = JESTER_COUNT.get();

            context.getMatrices().translate(0, 8 + ((civilianTotal + 3) / 4) * 12, 0);
            context.getMatrices().translate(-60 + (jesters % 4) * 12, 14 + (jesters / 4) * 12, 0);
            JESTER_COUNT.set(++jesters);
        }
    }

    @Inject(
            method = "renderHud",
            at = @At("TAIL")
    )
    private static void jester$clearCache(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, CallbackInfo ci) {
        CIVILIAN_TOTAL.remove();
        JESTER_COUNT.remove();
    }

}
