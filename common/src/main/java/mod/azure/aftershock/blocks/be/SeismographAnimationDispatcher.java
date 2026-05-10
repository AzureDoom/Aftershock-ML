package mod.azure.aftershock.blocks.be;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

public class SeismographAnimationDispatcher {

    private static final AzCommand idleCommand = AzCommand.create(
        "base_controller",
        "idle",
        AzPlayBehaviors.LOOP
    );

    private static final AzCommand movingComamnd = AzCommand.create(
        "base_controller",
        "moving",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final SeismographBlockEntity seismographBlockEntity;

    public SeismographAnimationDispatcher(SeismographBlockEntity seismographBlockEntity) {
        this.seismographBlockEntity = seismographBlockEntity;
    }

    public void sendIdleAnimation() {
        idleCommand.sendForBlockEntity(seismographBlockEntity);
    }

    public void sendMovingAnimation() {
        movingComamnd.sendForBlockEntity(seismographBlockEntity);
    }
}
