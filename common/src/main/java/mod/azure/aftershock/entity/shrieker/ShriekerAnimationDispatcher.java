package mod.azure.aftershock.entity.shrieker;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

public class ShriekerAnimationDispatcher {

    private final AzCommand idleCommand = AzCommand.create(
        "base_controller",
        "idle",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand agressiveCommand = AzCommand.create(
        "eyes_controller",
        "agressive_mode",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand idleModeCommand = AzCommand.create(
        "eyes_controller",
        "idle_mode",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand lookCommand = AzCommand.create(
        "base_controller",
        "look",
        AzPlayBehaviors.LOOP
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

    private final AzCommand eatingCommand = AzCommand.create(
        "base_controller",
        "grab",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand deathCommand = AzCommand.create(
        "base_controller",
        "death",
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final AzCommand statisStartCommand = AzCommand.create(
        "base_controller",
        "statis_start",
        AzPlayBehaviors.HOLD_ON_LAST_FRAME
    );

    private final AzCommand statisIdleCommand = AzCommand.create(
        "base_controller",
        "statis_idle",
        AzPlayBehaviors.LOOP
    );

    private final AzCommand biteCommand = AzCommand.create(
        "base_controller",
        "bite",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand hurtCommand = AzCommand.create(
        "base_controller",
        "hurt",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final AzCommand pukeCommand = AzCommand.create(
        "base_controller",
        "puke",
        AzPlayBehaviors.PLAY_ONCE
    );

    private final ShriekerEntity shriekerEntity;

    public ShriekerAnimationDispatcher(ShriekerEntity shriekerEntity) {
        this.shriekerEntity = shriekerEntity;
    }

    public void clientIdle() {
        idleCommand.sendForEntity(shriekerEntity);
    }

    public void clientAgressive() {
        agressiveCommand.sendForEntity(shriekerEntity);
    }

    public void clientIdleMode() {
        idleModeCommand.sendForEntity(shriekerEntity);
    }

    public void clientLook() {
        lookCommand.sendForEntity(shriekerEntity);
    }

    public void clientWalk() {
        walkCommand.sendForEntity(shriekerEntity);
    }

    public void clientRun() {
        runCommand.sendForEntity(shriekerEntity);
    }

    public void clientDeath() {
        deathCommand.sendForEntity(shriekerEntity);
    }

    public void clientHurt() {
        hurtCommand.sendForEntity(shriekerEntity);
    }

    public void serverBite() {
        biteCommand.sendForEntity(shriekerEntity);
    }

    public void statisStart() {
        statisStartCommand.sendForEntity(shriekerEntity);
    }

    public void statisIdle() {
        statisIdleCommand.sendForEntity(shriekerEntity);
    }

    public void serverEating() {
        eatingCommand.sendForEntity(shriekerEntity);
    }

    public void serverPuke() {
        pukeCommand.sendForEntity(shriekerEntity);
    }
}
