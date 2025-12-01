package net.sylviameows.jesterrole.cca;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class JesterWorldComponent implements AutoSyncedComponent {
    public static final ComponentKey<JesterWorldComponent> KEY =
            ComponentRegistry.getOrCreate(Identifier.of("jester", "world"), JesterWorldComponent.class);
    private final World world;


    private boolean enabled = true;
    private double chance = 1.0;

    private boolean winner = false;

    public JesterWorldComponent(World world) {
        this.world = world;
    }

    public void sync() {
        JesterWorldComponent.KEY.sync(this.world);
    }

    public void setChance(double chance) {
        this.chance = chance;
        sync();
    }

    public double getChance() {
        return this.chance;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void reset() {
        this.winner = false;
        rollChance();
    }

    public void rollChance() {
        this.enabled = Math.random() > (1-getChance());
        sync();
    }

    public void setJesterWin(boolean winner) {
        this.winner = winner;
        sync();
    }

    public boolean isJesterWin() {
        return winner;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.winner = tag.getBoolean("winner");
        this.enabled = tag.getBoolean("enabled");
        this.chance = tag.getDouble("chance");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("winner", winner);
        tag.putBoolean("enabled", enabled);
        tag.putDouble("chance", chance);
    }

    @Override
    public boolean isRequiredOnClient() {
        return false;
    }
}
