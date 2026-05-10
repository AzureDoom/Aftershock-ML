package mod.azure.aftershock.util;

import mod.azure.azurelib.common.registry.AzureBlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ItemUtils {

    public static void spawnLightSource(Entity entity, Boolean isInWaterBlock) {
        BlockPos lightBlockPos = null;
        lightBlockPos = findFreeSpace(entity.level(), entity.blockPosition());
        if (lightBlockPos == null)
            return;
        entity.level()
            .setBlockAndUpdate(lightBlockPos, AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get().defaultBlockState());
    }

    private static boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB) {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= 2 && Math.abs(blockPosA.getY() - blockPosB.getY()) <= 2
            && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= 2;
    }

    private static BlockPos findFreeSpace(Level world, BlockPos blockPos) {
        if (blockPos == null)
            return null;

        var offsets = new int[2 * 2 + 1];
        offsets[0] = 0;
        for (var i = 2; i <= 2 * 2; i += 2) {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (var x : offsets)
            for (var y : offsets)
                for (var z : offsets) {
                    var offsetPos = blockPos.offset(x, y, z);
                    var state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(AzureBlocksRegistry.TICKING_LIGHT_BLOCK.get()))
                        return offsetPos;
                }
        return null;
    }
}
