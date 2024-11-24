package com.crossvas.wantedomnitool.items;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.util.EnergyContainerItemWrapper;
import com.crossvas.wantedomnitool.WantedOmniTool;
import com.crossvas.wantedomnitool.items.features.IToolMode;
import com.crossvas.wantedomnitool.items.features.IToolProps;
import com.crossvas.wantedomnitool.items.features.ITorchPlacer;
import com.crossvas.wantedomnitool.utils.TextFormatter;
import com.crossvas.wantedomnitool.utils.ToolHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class ItemOmniTool extends Item implements IHasModel, IEnergyContainerItem, IToolProps, IToolMode, ITorchPlacer {

    public static int maxEnergy = 400000;
    public static int rechargeRate = 10000;

    protected ItemOmniTool() {
        setRegistryName(WantedOmniTool.MODID, "omnitool");
        ModItems.toRegister.add(this);
        setCreativeTab(CreativeTabs.TOOLS);
        setTranslationKey("omnitool");
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return ImmutableSet.of("pickaxe", "spade", "sword", "axe", "shears", "scoop");
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        IToolProps.ToolProps props = getProps(stack);
        if (getEnergyStored(stack) >= props.energyCost) {
            return props.efficiency;
        }
        return 0.0F;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return TextFormatter.RED.literal(super.getItemStackDisplayName(stack)).getFormattedText();
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatter.AQUA.translatable("tool.energy", getEnergyStored(stack), getMaxEnergyStored(stack)).getFormattedText());
        tooltip.add(getAOEComp(stack).getFormattedText());
        tooltip.add(getPropComp(stack).getFormattedText());
    }

    /**
     * Modes changer
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!world.isRemote) {
            if (WantedOmniTool.keyboard.isToolModeKeyDown(player)) {
                ToolMode mode = getNextMode(stack);
                saveMode(stack, mode);
                player.sendMessage(getAOEComp(stack));
            }

            if (WantedOmniTool.keyboard.isAltKeyDown(player)) {
                ToolProps props = getNextProps(stack);
                saveProps(stack, props);
                player.sendMessage(getPropComp(stack));
            }

            if (player.isSneaking()) {
                Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
                NBTTagList enchTagList = stack.getEnchantmentTagList();
                if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
                    enchantmentMap.put(Enchantments.SILK_TOUCH, 1);
                    player.sendMessage(TextFormatter.LIGHT_PURPLE.translatable("message.text.mode.mining", TextFormatter.GREEN.translatable("message.text.mode.silk")));
                } else {
                    enchantmentMap.put(Enchantments.FORTUNE, 3);
                    player.sendMessage(TextFormatter.LIGHT_PURPLE.translatable("message.text.mode.mining", TextFormatter.AQUA.translatable("message.text.mode.fortune")));
                }
                for (int i = 0; i < enchTagList.tagCount(); i++) {
                    int id = enchTagList.getCompoundTagAt(i).getInteger("id");
                    int lvl = enchTagList.getCompoundTagAt(i).getInteger("lvl");
                    if (!Objects.equals(Enchantment.getEnchantmentByID(id), Enchantments.FORTUNE)
                            && !Objects.equals(Enchantment.getEnchantmentByID(id), Enchantments.SILK_TOUCH)) {
                        enchantmentMap.put(Enchantment.getEnchantmentByID(id), lvl);
                    }
                }
                EnchantmentHelper.setEnchantments(enchantmentMap, stack);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        }
        return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    /**
     * Torch Placer - for all items
     */
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        return placeTorch(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos originPos, EntityPlayer player) {
        if (!player.world.isRemote) {
            World world = player.world;
            ToolMode mode = getMode(stack);
            ToolProps props = getProps(stack);
            if (mode == ToolMode.BIG_HOLES) {
                ImmutableList<BlockPos> ares = getBreakableBlocksRadius(originPos, player);
                for (BlockPos pos : ares)
                    if (getEnergyStored(stack) >= props.energyCost) {
                        ToolHelper.breakBlock(stack, player.world, player.world.getBlockState(pos), pos, player);
                    }
            }
            if (mode == ToolMode.VEIN || mode == ToolMode.VEIN_EXTENDED) {
                IBlockState state = world.getBlockState(originPos);
                Block block = state.getBlock();
                RayTraceResult rayTrace = rayTrace(world, player, false);
                ItemStack blockStack = block.getPickBlock(state, rayTrace, world, originPos, player);
                List<String> oreNames = new ArrayList<>();
                if (!blockStack.isEmpty()) {
                    int[] oreIDs = OreDictionary.getOreIDs(blockStack);
                    for (Integer id : oreIDs) {
                        oreNames.add(OreDictionary.getOreName(id));
                    }
                }

                boolean isOre = false;
                for (String name : oreNames) {
                    if (name.startsWith("ore") || name.startsWith("log")) {
                        isOre = true;
                        break;
                    }
                }
                boolean veinGeneral = (mode == ToolMode.VEIN && isOre) || (mode == ToolMode.VEIN_EXTENDED);
                if (!player.capabilities.isCreativeMode && veinGeneral && !player.isSneaking()
                        && getEnergyStored(stack) >= props.energyCost) {
                    for (BlockPos coord : findPositions(state, originPos, world, player.isSneaking() ? 0 : 128)) {
                        if (coord.equals(originPos)) {
                            continue;
                        }
                        int experience;
                        if (player instanceof EntityPlayerMP) {
                            experience = ForgeHooks.onBlockBreakEvent(world,
                                    ((EntityPlayerMP) player).interactionManager.getGameType(), (EntityPlayerMP) player,
                                    coord);
                            if (experience < 0)
                                return false;
                        } else {
                            experience = 0;
                        }
                        Block block2 = world.getBlockState(coord).getBlock();
                        block2.onBlockHarvested(world, coord, state, player);
                        if (player.isCreative()) {
                            if (block2.removedByPlayer(state, world, coord, player, false))
                                block2.breakBlock(world, coord, state);
                        } else {
                            if (block2.removedByPlayer(state, world, coord, player, true)) {
                                block2.breakBlock(world, coord, state);
                                block2.harvestBlock(world, player, coord, state, world.getTileEntity(coord), stack);
                                if (experience > 0)
                                    block2.dropXpOnBlockBreak(world, coord, experience);
                            }
                            stack.onBlockDestroyed(world, state, coord, player);
                        }
                        world.playEvent(2001, coord, Block.getStateId(state));
                    }
                }
            }
        }
        return false;
    }

    public int getEnergyUsage(ItemStack stack) {
        return getProps(stack).energyCost;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            ItemStack empty = new ItemStack(this);
            ItemStack full = new ItemStack(this);
            setEnergy(full, Integer.MAX_VALUE);
            items.add(empty);
            items.add(full);
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        drainEnergy(stack, getEnergyUsage(stack), target);
        return ToolHelper.shearEntity(stack, playerIn, target);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return EnumEnchantmentType.DIGGER.equals(enchantment.type)
                || EnumEnchantmentType.WEAPON.equals(enchantment.type);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return Items.DIAMOND_SWORD.isBookEnchantable(stack, book)
                || Items.DIAMOND_PICKAXE.isBookEnchantable(stack, book);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    public void drainEnergy(ItemStack stack, int energy, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)
            return;
        if (getEnergyStored(stack) >= getEnergyUsage(stack)) {
            setEnergy(stack, Math.max(getEnergyStored(stack) - energy, 0));
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                ForgeEventFactory.onPlayerDestroyItem(player, stack, entity.getActiveHand());
                player.addStat(StatList.getObjectBreakStats(this));
            }
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0d - (double) getEnergyStored(stack) / (double) getMaxEnergyStored(stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos,
                                    EntityLivingBase entity) {
        drainEnergy(stack, getEnergyUsage(stack), entity);
        return true;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        int energyUse = getEnergyUsage(stack) * 2;
        if (getEnergyStored(stack) > energyUse) {
            if (attacker instanceof EntityPlayer) {
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 20.0F);
            } else {
                target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 20.0F);
            }
            drainEnergy(stack, energyUse, attacker);
        }
        return false;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    /**
     * {@link IHasModel}
     */
    @Override
    public void registerModel() {
        WantedOmniTool.proxy.registerItemRenderer(this, 0);
    }

    public void setEnergy(ItemStack stack, int energy) {
        stack.setTagInfo("Energy", new NBTTagInt(Math.min(energy, getMaxEnergyStored(stack))));
    }

    /**
     * {@link IEnergyContainerItem}
     */
    @Override
    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        int energy = getEnergyStored(stack);
        int energyReceived = Math.min(maxEnergy - energy, Math.min(rechargeRate, maxReceive));

        if (!simulate)
            energy += energyReceived;
        assert stack.getTagCompound() != null;
        stack.getTagCompound().setInteger("Energy", energy);
        return energyReceived;
    }

    @Override
    public int extractEnergy(ItemStack itemStack, int i, boolean b) {
        return 0;
    }

    @Override
    public int getEnergyStored(ItemStack stack) {
        if (stack.hasTagCompound()) {
            assert stack.getTagCompound() != null;
            return stack.getTagCompound().getInteger("Energy");
        } else return 0;
    }

    @Override
    public int getMaxEnergyStored(ItemStack itemStack) {
        return maxEnergy;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new EnergyContainerItemWrapper(stack, this);
    }

    @Override
    public int getRadius(EntityPlayer player) {
        return player.isSneaking() ? 0 : 1;
    }
}