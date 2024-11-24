package com.crossvas.wantedomnitool.items.features;

import com.crossvas.wantedomnitool.utils.MiscUtil;
import com.crossvas.wantedomnitool.utils.TextFormatter;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface IToolMode {

    int getRadius(EntityPlayer player);

    default ITextComponent getAOEComp(ItemStack stack) {
        ToolMode mode = getMode(stack);
        return TextFormatter.GOLD.translatable("message.text.mode", mode.formattedName);
    }

    default ToolMode getMode(ItemStack drill) {
        NBTTagCompound tag = MiscUtil.getTag(drill);
        return ToolMode.getFromId(tag.getByte("toolMode"));
    }

    default ToolMode getNextMode(ItemStack drill) {
        NBTTagCompound tag = MiscUtil.getTag(drill);
        return ToolMode.getFromId(tag.getByte("toolMode") + 1);
    }

    default void saveMode(ItemStack drill, ToolMode mode) {
        NBTTagCompound tag = MiscUtil.getTag(drill);
        tag.setByte("toolMode", (byte) mode.ordinal());
    }

    default ImmutableList<BlockPos> getBreakableBlocksRadius(BlockPos origin, EntityPlayer player) {
        List<BlockPos> area = new ArrayList<>();
        World world = player.world;
        int radius = getRadius(player);
        RayTraceResult traceResult = rayTrace(world, player, false);
        if (traceResult.typeOfHit == RayTraceResult.Type.MISS || player.isSneaking() || radius <= 0) {
            return ImmutableList.of();
        }

        int xRange = radius, yRange = radius, zRange = radius;
        switch (traceResult.sideHit) {
            case DOWN: case UP:
                yRange = 0;
                break;
            case NORTH: case SOUTH:
                zRange = 0;
                break;
            default:
                xRange = 0;
                break;
        }

        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-xRange, -yRange, -zRange), origin.add(xRange, yRange, zRange))) {
            if (canToolAffect(player, pos, origin) && pos != origin) {
                area.add(pos.toImmutable());
            }
        }

        return ImmutableList.copyOf(area);
    }

    static RayTraceResult rayTrace(World world, EntityPlayer player, boolean useLiquids) {
        float rotationPitch = player.rotationPitch;
        float rotationYaw = player.rotationYaw;
        double posX = player.posX;
        double eyePos = player.posY + (double) player.getEyeHeight();
        double posZ = player.posZ;
        Vec3d lookVector = new Vec3d(posX, eyePos, posZ);
        float cossed = MathHelper.cos(-rotationYaw * 0.017453292F - (float) Math.PI);
        float sinned = MathHelper.sin(-rotationYaw * 0.017453292F - (float) Math.PI);
        float sinnedRotation = -MathHelper.cos(-rotationPitch * 0.017453292F);
        float cossedRotation = MathHelper.sin(-rotationPitch * 0.017453292F);
        float f6 = sinned * sinnedRotation;
        float f7 = cossed * sinnedRotation;
        double reachDistance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        Vec3d traceVector = lookVector.add((double) f6 * reachDistance, (double) cossedRotation * reachDistance, (double) f7 * reachDistance);
        RayTraceResult res = world.rayTraceBlocks(lookVector, traceVector, useLiquids, !useLiquids, false);
        return res != null ? res : new RayTraceResult(RayTraceResult.Type.MISS, Vec3d.ZERO, EnumFacing.UP, BlockPos.ORIGIN);
    }

    static boolean canToolAffect(EntityPlayer player, BlockPos offset, BlockPos origin) {
        World world = player.world;
        IBlockState originState = world.getBlockState(origin);
        IBlockState offsetState = world.getBlockState(offset);
        float alphaStrength = originState.getBlockHardness(world, origin);
        float strength = offsetState.getBlockHardness(world, offset);
        boolean originHarvest = ForgeHooks.canHarvestBlock(originState.getBlock(), player, world, origin);
        boolean offsetHarvest = ForgeHooks.canHarvestBlock(offsetState.getBlock(), player, world, origin);
        return (originHarvest || offsetHarvest) && strength > 0.0F && strength / alphaStrength <= 8.0F;
    }

    default Set<BlockPos> findPositions(IBlockState state, BlockPos location, World world, int maxRange) {
        Block startBlock = state.getBlock();
        Set<BlockPos> foundSet = new LinkedHashSet<>();
        Set<BlockPos> openSet = new LinkedHashSet<>();
        openSet.add(location);
        while (!openSet.isEmpty()) {
            BlockPos blockPos = openSet.iterator().next();
            foundSet.add(blockPos); // add blockPos to found list for return
            openSet.remove(blockPos); // remove it and continue
            if (foundSet.size() > maxRange) { // nah, too much
                return foundSet;
            }
            Iterable<BlockPos> area = BlockPos.getAllInBox(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1));
            for (BlockPos pos : area) {
                if (!foundSet.contains(pos)) {
                    IBlockState blockState = world.getBlockState(pos);
                    if (!blockState.getBlock().equals(Blocks.AIR)) {
                        if (blockState.getBlock().equals(startBlock)) {
                            // Make sure to add it as immutable
                            openSet.add(pos.toImmutable());
                        }
                    }
                }
            }
        }
        return foundSet;
    }

    enum ToolMode {
        NORMAL(TextFormatter.DARK_GREEN),
        BIG_HOLES(TextFormatter.LIGHT_PURPLE),
        VEIN(TextFormatter.BLUE),
        VEIN_EXTENDED(TextFormatter.RED);

        private static final ToolMode[] VALUES = values();
        public final ITextComponent formattedName;

        ToolMode(TextFormatter color) {
            this.formattedName = color.translatable("message.text.mode." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name()));
        }

        public static ToolMode getFromId(int ID) {
            return VALUES[ID % VALUES.length];
        }
    }
}
