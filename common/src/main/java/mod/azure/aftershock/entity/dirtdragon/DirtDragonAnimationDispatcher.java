package mod.azure.aftershock.entity.dirtdragon;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

public class DirtDragonAnimationDispatcher {

    private final AzCommand idleCommand = AzCommand.create(
        "base_controller",
        "idle",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand walkCommand = AzCommand.create(
        "base_controller",
        "moving",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand hurtCommand = AzCommand.create(
        "base_controller",
        "hurt",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand holdCommand = AzCommand.create(
        "base_controller",
        "hold",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand draggingCommand = AzCommand.create(
        "base_controller",
        "dragging",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand deathCommand = AzCommand.create(
        "base_controller",
        "death",
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final DirtDragonEntity dirtDragonEntity;

    public DirtDragonAnimationDispatcher(DirtDragonEntity dirtDragonEntity) {
        this.dirtDragonEntity = dirtDragonEntity;
    }

    public void clientIdle() {
        idleCommand.sendForEntity(dirtDragonEntity);
    }

    public void clientWalk() {
        walkCommand.sendForEntity(dirtDragonEntity);
    }

    public void clientHurt() {
        hurtCommand.sendForEntity(dirtDragonEntity);
    }

    public void clientHold() {
        holdCommand.sendForEntity(dirtDragonEntity);
    }

    public void clientDeath() {
        deathCommand.sendForEntity(dirtDragonEntity);
    }

    public void serverDragging() {
        draggingCommand.sendForEntity(dirtDragonEntity);
    }
}
