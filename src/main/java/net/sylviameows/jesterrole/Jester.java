package net.sylviameows.jesterrole;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.sylviameows.jesterrole.commands.RoleChanceCommand;
import net.sylviameows.jesterrole.mixin.MPlayerMoodAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class Jester implements ModInitializer {
    public static final String MOD_ID = "jester";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CHANNEL = id("verify");

    public static final List<UUID> forcedJesters = new ArrayList<>();

    public static @NotNull Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    public static boolean ENABLED = true;
    public static double CHANCE = 1.0;

    public static final int ROLE_COLOR = 0xF8C8DC;
    public static Role ROLE = new Role(
            Jester.id("role"),
            ROLE_COLOR,
            false,
            false,
            Role.MoodType.REAL,
            GameConstants.getInTicks(0, 10),
            false
    );
    public static RoleAnnouncementTexts.RoleAnnouncementText TEXT = new JesterAnnouncementText();

    private static boolean JESTER_WIN = false;

    public static boolean isJesterWin() {
        return JESTER_WIN;
    }

    public static void setJesterWin(boolean jesterWin, @Nullable List<ServerPlayerEntity> players) {
        if (players != null) {
            players.forEach(player -> ServerPlayNetworking.send(player, new JesterWinPayload(jesterWin)));
        }
        JESTER_WIN = jesterWin;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Attempting role injection.");
        TMMRoles.registerRole(ROLE);
        RoleAnnouncementTexts.registerRoleAnnouncementText(TEXT);

        PayloadTypeRegistry.playS2C().register(JesterWinPayload.ID, JesterWinPayload.CODEC);

        ServerLoginConnectionEvents.QUERY_START.register(((handler, server, sender, synchronizer) -> {
            ServerLoginNetworking.registerReceiver(handler, CHANNEL, (_server, _handler, understood, buf, _synchronizer, responder) -> {
                if (!understood) {
                    handler.disconnect(Text.literal("")
                            .append(Text.literal("ᴍɪssɪɴɢ ᴍᴏᴅ").withColor(ROLE_COLOR).formatted(Formatting.BOLD))
                            .append("\n\nYou must have the Jester mod installed to play on this server.\n\n")
                            .append(Text.literal("Find it here:\n").formatted(Formatting.GRAY))
                            .append(Text.literal("https://sylviameo.ws/projects/jester/").formatted(Formatting.UNDERLINE))
                            .append(Text.literal("\n\n\n"))
                    );
                    return;
                }

                String version = buf.readString();
                LOGGER.info("client joining with jester version {}", version);

                _synchronizer.waitFor(CompletableFuture.completedFuture(null));
            });

            PacketByteBuf out = PacketByteBufs.create();
            sender.sendPacket(CHANNEL, out.writeBoolean(isJesterWin()));
        }));

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            RoleChanceCommand.register(dispatcher);
        }));


        Jester.LOGGER.info("Role successfully injected.");
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
