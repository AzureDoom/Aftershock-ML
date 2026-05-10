package mod.azure.aftershock.blocks;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum SeismographStates implements StringRepresentable {

    OPENED("opened"),
    CLOSED("closed");

    private final String name;

    private SeismographStates(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

}
