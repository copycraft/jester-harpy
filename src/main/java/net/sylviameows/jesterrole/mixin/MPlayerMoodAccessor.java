package net.sylviameows.jesterrole.mixin;

import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoodComponent.class)
public interface MPlayerMoodAccessor {
    @Accessor(value = "mood")
    float jester$getRealMood();
}
