package net.sylviameows.jesterrole;

import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.sylviameows.jesterrole.cca.JesterWorldComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JesterAnnouncementText extends RoleAnnouncementTexts.RoleAnnouncementText {
    public JesterAnnouncementText() {
        super("jester", Jester.ROLE_COLOR);
    }

    @Override
    public @Nullable Text getEndText(GameFunctions.@NotNull WinStatus status, Text winner) {
        World world = MinecraftClient.getInstance().world;
        if (world != null) {
            JesterWorldComponent component = JesterWorldComponent.KEY.get(MinecraftClient.getInstance().world);
            if (component.isJesterWin()) {
                return winText;
            }
        }

        return switch (status) {
            case NONE -> null;
            case PASSENGERS, TIME -> RoleAnnouncementTexts.CIVILIAN.winText;
            case KILLERS -> RoleAnnouncementTexts.KILLER.winText;
            case LOOSE_END ->
                    Text.translatable("announcement.win." + "loose_end", winner).withColor(RoleAnnouncementTexts.LOOSE_END.colour);
        };
    }
}
