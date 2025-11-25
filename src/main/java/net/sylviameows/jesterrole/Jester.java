package net.sylviameows.jesterrole;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.sylviameows.jesterrole.mixin.MPlayerMoodAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


public class Jester implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "jester";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static @NotNull Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    public static boolean ENABLED = true;

    public static final int ROLE_COLOR = 0xF8C8DC;
    public static TMMRoles.Role ROLE = new TMMRoles.Role(Jester.id("role"), ROLE_COLOR, false, false);
    public static RoleAnnouncementTexts.RoleAnnouncementText TEXT = new RoleAnnouncementTexts.RoleAnnouncementText("jester", ROLE_COLOR);

    private static boolean JESTER_WIN = false;

    public static boolean isJesterWin() {
        return JESTER_WIN;
    }

    public static void setJesterWin(boolean jesterWin, @Nullable List<ServerPlayerEntity> players) {
        if (players != null) {
            players.forEach(player -> ServerPlayNetworking.send(player, new JesterPayload(jesterWin)));
        }
        JESTER_WIN = jesterWin;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Attempting role injection.");
        TMMRoles.registerRole(ROLE);
        RoleAnnouncementTexts.registerRoleAnnouncementText(TEXT);

        PayloadTypeRegistry.playS2C().register(JesterPayload.ID, JesterPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, play) -> {
            ServerPlayNetworking.send(handler.player, new JesterPayload(isJesterWin()));
        });

        Jester.LOGGER.info("Role successfully injected.");
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(JesterPayload.ID, new JesterPayload.Receiver());
    }

    public static void lockpickUse(World world, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (game.isRole(player, Jester.ROLE)) {
            var moodComponent = PlayerMoodComponent.KEY.get(player);
            float mood = ((MPlayerMoodAccessor) moodComponent).jester$getRealMood();
            if (mood < 0.24f) {
                player.sendMessage(Text.translatable("tip.lockpick.not_sane"), true);
                cir.setReturnValue(ActionResult.PASS);
                cir.cancel();
                return;
            }
            mood -= 0.25f;
            if (mood < 0.15f) mood = 0f;
            moodComponent.setMood(mood);
        }
    }
}
