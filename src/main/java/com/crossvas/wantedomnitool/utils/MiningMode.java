package com.crossvas.wantedomnitool.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class MiningMode {

    public static final MiningMode NORMAL = new MiningMode(0);
    public static final MiningMode VEIN = new MiningMode(0);
    public static final MiningMode VEIN_EXTENDED = new MiningMode(0);
    public static final MiningMode BIG_HOLES = new MiningMode(1);

    public final int radius;

    public MiningMode(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return String.format("Radius:" + " " + "%s", radius);
    }

    public ImmutableList<BlockPos> getAoEBlocks(World world, EntityPlayer player, BlockPos pos,
                                                EnumFacing side, boolean harvest) {
        if (harvest && !ForgeHooks.canHarvestBlock(world.getBlockState(pos).getBlock(), player, world, pos))
            return ImmutableList.of();
        int xRadius = radius, yRadius = radius, zRadius = radius;
        BlockPos center = pos.offset(side.getOpposite(), 0);

        switch (side.getAxis()) {
            case X:
                xRadius = 0;
                break;
            case Y:
                yRadius = 0;
                break;
            case Z:
                zRadius = 0;
                break;
        }

        ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
        for (int x = center.getX() - xRadius; x <= center.getX() + xRadius; x++) {
            for (int y = center.getY() - yRadius; y <= center.getY() + yRadius; y++) {
                for (int z = center.getZ() - zRadius; z <= center.getZ() + zRadius; z++) {
                    BlockPos harvestPos = new BlockPos(x, y, z);
                    if (harvestPos.equals(pos))
                        continue;
                    IBlockState state = world.getBlockState(harvestPos);
                    if (!harvest || ToolHelper.canAoEHarvest(world, state, harvestPos, pos, player))
                        builder.add(new BlockPos(x, y, z));
                }
            }
        }
        return builder.build();
    }
}
