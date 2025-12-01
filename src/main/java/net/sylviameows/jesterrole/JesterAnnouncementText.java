package net.sylviameows.jesterrole;

import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JesterAnnouncementText extends RoleAnnouncementTexts.RoleAnnouncementText {
    public JesterAnnouncementText() {
        super("jester", Jester.ROLE_COLOR);
    }

    public @Nullable Text getEndText(GameFunctions.@NotNull WinStatus status, Text winner) {
        return switch (status) {
            case NONE -> null;
            case PASSENGERS, TIME -> RoleAnnouncementTexts.CIVILIAN.winText;
            case KILLERS -> RoleAnnouncementTexts.KILLER.winText;
            case LOOSE_END ->
                    super.getEndText(status, winner);
        };
    }
}
