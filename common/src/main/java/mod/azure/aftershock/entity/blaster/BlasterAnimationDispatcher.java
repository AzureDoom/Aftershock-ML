package mod.azure.aftershock.entity.blaster;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

public class BlasterAnimationDispatcher {

    private final AzCommand idleCommand = AzCommand.create(
        "base_controller",
        "idle",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand lookCommand = AzCommand.create(
        "base_controller",
        "look",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand wakeOffCommand = AzCommand.create(
        "base_controller",
        "wake",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand walkCommand = AzCommand.create(
        "base_controller",
        "moving",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand runCommand = AzCommand.create(
        "base_controller",
        "running",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand takeOffCommand = AzCommand.create(
        "base_controller",
        "take_off",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand deathCommand = AzCommand.create(
        "base_controller",
        "death",
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final AzCommand eatingCommand = AzCommand.create(
        "base_controller",
        "eating",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand biteCommand = AzCommand.create(
        "base_controller",
        "bite",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand layEggCommand = AzCommand.create(
        "base_controller",
        "laying_egg",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand standUpEggCommand = AzCommand.create(
        "base_controller",
        "unlaying_egg",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand glidingCommand = AzCommand.create(
        "base_controller",
        "gliding",
        AzPlayBehaviors.LOOP
    );

    private final BlasterEntity blasterEntity;

    public BlasterAnimationDispatcher(BlasterEntity blasterEntity) {
        this.blasterEntity = blasterEntity;
    }

    public void clientIdle() {
        idleCommand.sendForEntity(blasterEntity);
    }

    public void clientLook() {
        lookCommand.sendForEntity(blasterEntity);
    }

    public void clientWalk() {
        walkCommand.sendForEntity(blasterEntity);
    }

    public void clientRun() {
        runCommand.sendForEntity(blasterEntity);
    }

    public void clientDeath() {
        deathCommand.sendForEntity(blasterEntity);
    }

    public void serverEating() {
        eatingCommand.sendForEntity(blasterEntity);
    }

    public void serverBite() {
        biteCommand.sendForEntity(blasterEntity);
    }

    public void serverGliding() {
        glidingCommand.sendForEntity(blasterEntity);
    }

    public void serverTakeOff() {
        takeOffCommand.sendForEntity(blasterEntity);
    }

    public void serverLayEgg() {
        layEggCommand.sendForEntity(blasterEntity);
    }

    public void serverStandUp() {
        standUpEggCommand.sendForEntity(blasterEntity);
    }

    public void serverWake() {
        wakeOffCommand.sendForEntity(blasterEntity);
    }
}
