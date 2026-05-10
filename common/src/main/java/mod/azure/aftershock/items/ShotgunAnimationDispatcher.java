package mod.azure.aftershock.items;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class ShotgunAnimationDispatcher {

    private static final AzCommand firingCommand = AzCommand.create(
        "base_controller",
        "firing",
        AzPlayBehaviors.PLAY_ONCE
    );

    private static final AzCommand reloadCommand = AzCommand.create(
        "base_controller",
        "reload",
        AzPlayBehaviors.PLAY_ONCE
    );

    public void serverFire(Entity entity, ItemStack itemStack) {
        firingCommand.sendForItem(entity, itemStack);
    }

    public void serverReload(Entity entity, ItemStack itemStack) {
        reloadCommand.sendForItem(entity, itemStack);
    }
}
