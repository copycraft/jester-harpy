package net.sylviameows.jesterrole.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.client.gui.MoodRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.sylviameows.jesterrole.Jester;
import net.sylviameows.jesterrole.mixin.MPlayerMoodAccessor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MoodRenderer.class)
public abstract class MMoodRenderer {

    @Unique
    private static final Identifier MOOD_JESTER = Jester.id("hud/mood_jester");

    @Shadow
    public static float moodRender = 0f;
    @Shadow
    public static float moodOffset = 0f;
    @Shadow
    public static float moodTextWidth = 0f;
    @Shadow
    public static float moodAlpha = 0f;

    @Invoker("renderCivilian")
    protected static void jester$renderCivilian(@NotNull TextRenderer textRenderer, @NotNull DrawContext context, float prevMood) {
        throw new AssertionError();
    }

    @Redirect(
            method = "renderHud",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/PlayerMoodComponent;getMood()F", ordinal = 0)
    )
    private static float jester$getMood(PlayerMoodComponent instance, @Local(name = "gameWorldComponent") GameWorldComponent game) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && game.isRole(player, Jester.ROLE)) return ((MPlayerMoodAccessor) instance).jester$getRealMood();
        return instance.getMood();
    }

    @Redirect(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/trainmurdermystery/client/gui/MoodRenderer;renderCivilian(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/gui/DrawContext;F)V"
            )
    )
    private static void renderJester(TextRenderer textRenderer, DrawContext context, float prevMood, @Local(name = "gameWorldComponent") GameWorldComponent game, @Local(name = "player") PlayerEntity player) {
        if (game.isRole(player, Jester.ROLE)) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 3 * moodOffset, 0);
            context.drawGuiTexture(MOOD_JESTER, 5, 6, 14, 17);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().translate(0, 10 * moodOffset, 0);
            context.getMatrices().translate(26, 8 + textRenderer.fontHeight, 0);
            context.getMatrices().scale((moodTextWidth - 8) * moodRender, 1, 1);
            context.fill(0, 0, 1, 1, Jester.ROLE_COLOR | ((int) (Math.max(moodAlpha, 0.25f) * 255) << 24));
            context.getMatrices().pop();
        } else {
            jester$renderCivilian(textRenderer, context, prevMood);
        }
    }

}
