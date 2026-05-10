package mod.azure.aftershock.entity.graboid;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

public class GraboidAnimationDispatcher {

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

    private final AzCommand deathCommand = AzCommand.create(
        "base_controller",
        "death",
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final AzCommand burrowCommand = AzCommand.create(
        "base_controller",
        "digin",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand grabOneCommand = AzCommand.create(
        "base_controller",
        "grab1",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand grabTwoCommand = AzCommand.create(
        "base_controller",
        "grab2",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand eruptingCommand = AzCommand.create(
        "base_controller",
        "erupting",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand biteCommand = AzCommand.create(
        "base_controller",
        "bite",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final GraboidEntity graboidEntity;

    public GraboidAnimationDispatcher(GraboidEntity graboidEntity) {
        this.graboidEntity = graboidEntity;
    }

    public void clientIdle() {
        idleCommand.sendForEntity(graboidEntity);
    }

    public void clientWalk() {
        walkCommand.sendForEntity(graboidEntity);
    }

    public void clientHurt() {
        hurtCommand.sendForEntity(graboidEntity);
    }

    public void clientDeath() {
        deathCommand.sendForEntity(graboidEntity);
    }

    public void serverBurrow() {
        burrowCommand.sendForEntity(graboidEntity);
    }

    public void serverGrabOne() {
        grabOneCommand.sendForEntity(graboidEntity);
    }

    public void serverGrabTwo() {
        grabTwoCommand.sendForEntity(graboidEntity);
    }

    public void serverErupting() {
        eruptingCommand.sendForEntity(graboidEntity);
    }

    public void serverBite() {
        biteCommand.sendForEntity(graboidEntity);
    }
}
