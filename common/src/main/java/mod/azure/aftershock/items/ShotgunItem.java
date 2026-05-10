package mod.azure.aftershock.items;

import mod.azure.azurelib.common.util.client.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.registry.ItemRegistry;
import mod.azure.aftershock.registry.SoundRegistry;
import mod.azure.aftershock.util.ItemUtils;

public class ShotgunItem extends Item {

    ShotgunAnimationDispatcher dispatcher;

    public ShotgunItem() {
        super(new Properties().stacksTo(1).component(CommonMod.AMMO_COUNT.get(), 0));
        this.dispatcher = new ShotgunAnimationDispatcher();
    }

    @Override
    public void onUseTick(
        @NotNull Level level,
        @NotNull LivingEntity livingEntity,
        @NotNull ItemStack stack,
        int remainingUseDuration
    ) {
        if (livingEntity instanceof Player player && !player.getCooldowns().isOnCooldown(stack.getItem())) {
            var ammo = getAmmo(stack);

            if (ammo > 0) {
                var hitResult = ProjectileUtil.getHitResultOnViewVector(player, entity -> true, 35);

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    var blockHitResult = (BlockHitResult) hitResult;
                    var blockPos = blockHitResult.getBlockPos();

                    if (!level.getBlockState(blockPos).isSolidRender(level, blockPos)) {
                        var hitVec = blockHitResult.getLocation();
                        var direction = blockHitResult.getLocation()
                            .subtract(player.getEyePosition(1.0f))
                            .normalize()
                            .multiply(35, 35, 35);

                        var nextBlockPos = blockPos.offset(
                            (int) direction.x(),
                            (int) direction.y(),
                            (int) direction.z()
                        );

                        hitResult = new BlockHitResult(hitVec, blockHitResult.getDirection(), nextBlockPos, false);
                    }
                }

                fire(level, player, stack, hitResult);

                if (!level.isClientSide() && !player.isCreative()) {
                    setAmmo(stack, ammo - 1);
                }
            } else {
                if (!level.isClientSide()) {
                    reloadFromInventory(level, player, stack);
                }
            }
        }

        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
        @NotNull Level world,
        Player user,
        @NotNull InteractionHand hand
    ) {
        final var itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext context,
        List<Component> tooltipComponents,
        @NotNull TooltipFlag tooltipFlag
    ) {
        var ammo = getAmmo(stack);

        tooltipComponents.add(
            Component.translatable("aftershock.eightgauge.ammo", ammo)
                .withStyle(ChatFormatting.ITALIC)
        );
        tooltipComponents.add(
            Component.translatable("aftershock.portable.text2")
                .withStyle(ChatFormatting.WHITE)
                .withStyle(ChatFormatting.ITALIC)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(Items.IRON_INGOT);
    }

    private void fire(@NotNull Level level, @NotNull Player player, ItemStack itemStack, HitResult hitResult) {
        damageEntity(level, player, hitResult);
        damageBlock(level, player, hitResult);
        dispatcher.serverFire(player, itemStack);
        player.getCooldowns().addCooldown(itemStack.getItem(), 5);
        level.playSound(player, player.blockPosition(), SoundRegistry.SHOTGUN.get(), SoundSource.PLAYERS, 1F, 1F);
        ItemUtils.spawnLightSource(player, player.level().isWaterAt(player.blockPosition()));
        if (level.isClientSide()) {
            ClientUtils.getClientPlayer().turn(getRecoilX(player) * 5, -getRecoilY() * 5);
        }
    }

    private void reloadFromInventory(@NotNull Level level, @NotNull Player player, ItemStack shotgunStack) {
        int ammo = getAmmo(shotgunStack);

        if (ammo >= 2) {
            return;
        }

        if (player.isCreative()) {
            setAmmo(shotgunStack, 2);
            reload(level, player, shotgunStack);
            player.getCooldowns().addCooldown(shotgunStack.getItem(), 20);
            return;
        }

        var shellItem = ItemRegistry.SHOTGUN_SHELL.get();
        var ammoToLoad = 2 - ammo;
        var loaded = 0;

        for (var i = 0; i < player.getInventory().getContainerSize(); i++) {
            var inventoryStack = player.getInventory().getItem(i);

            while (!inventoryStack.isEmpty() && inventoryStack.is(shellItem) && loaded < ammoToLoad) {
                inventoryStack.shrink(1);
                loaded++;
            }

            if (loaded >= ammoToLoad) {
                break;
            }
        }

        if (loaded > 0) {
            setAmmo(shotgunStack, ammo + loaded);
            reload(level, player, shotgunStack);
            player.getCooldowns().addCooldown(shotgunStack.getItem(), 20);
        }
    }

    private int getAmmo(ItemStack stack) {
        return stack.getOrDefault(CommonMod.AMMO_COUNT.get(), 0);
    }

    private void setAmmo(ItemStack stack, int ammo) {
        stack.set(CommonMod.AMMO_COUNT.get(), Math.clamp(ammo, 0, 2));
    }

    private void reload(@NotNull Level level, @NotNull Player player, ItemStack itemStack) {
        dispatcher.serverReload(player, itemStack);
        if (level instanceof ServerLevel serverLevel)
            serverLevel.playSound(
                player,
                player.blockPosition(),
                SoundRegistry.SHOTGUN_RELOAD.get(),
                SoundSource.PLAYERS,
                1F,
                1F
            );
    }

    public static float getRecoilX(Player player) {
        var baseRecoilX = player.level().getRandom().nextBoolean() ? 1f : -1f;
        return baseRecoilX / 2;
    }

    public static float getRecoilY() {
        return 2.5f / 2;
    }

    public static void damageEntity(@NotNull Level level, Player player, HitResult hitResult) {
        if (
            hitResult.getType() == HitResult.Type.ENTITY &&
                ((EntityHitResult) hitResult).getEntity() instanceof LivingEntity livingEntity
        ) {
            livingEntity.invulnerableTime = 0;
            livingEntity.hurt(level.damageSources().playerAttack(player), 5F);
        }
    }

    public static void damageBlock(@NotNull Level level, Player player, HitResult hitResult) {
        if (level.isClientSide()) {
            return;
        }

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        var blockHitResult = (BlockHitResult) hitResult;
        var blockPos = blockHitResult.getBlockPos();
        var blockState = level.getBlockState(blockPos);

        if (blockState.isAir()) {
            return;
        }

        var destroySpeed = blockState.getDestroySpeed(level, blockPos);

        if (destroySpeed < 0.0F) {
            return;
        }

        var maxHardness = 3.0F;

        if (destroySpeed > maxHardness) {
            return;
        }

        if (!player.mayBuild()) {
            return;
        }

        level.destroyBlock(blockPos, true, player);
    }
}
