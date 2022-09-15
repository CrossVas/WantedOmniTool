package com.crossvas.wantedomnitool.items;

import static net.minecraft.item.ItemBlock.setTileEntityNBT;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.util.EnergyContainerItemWrapper;
import com.crossvas.wantedomnitool.WantedOmniTool;
import com.crossvas.wantedomnitool.proxy.ClientProxy;
import com.crossvas.wantedomnitool.utils.EnumString;
import com.crossvas.wantedomnitool.utils.MiscUtil;
import com.crossvas.wantedomnitool.utils.ToolHelper;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class ItemOmniTool extends Item implements IHasModel, IEnergyContainerItem {

	public static int maxEnergy = 400000;
	public static int rechargeRate = 10000;

	public enum ToolProps {
		NORMAL(35.0F, 160, TextFormatting.BLUE), LOW(16.0F, 80, TextFormatting.GREEN),
		FINE(10.0F, 50, TextFormatting.AQUA);

		private static final ToolProps[] VALUES = values();
		public final String name;
		public final TextFormatting color;
		public final float efficiency;
		public final int energyCost;

		ToolProps(float efficiency, int energyCost, TextFormatting color) {
			this.name = EnumString.toolEfficiency + "."
					+ CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
			this.color = color;
			this.efficiency = efficiency;
			this.energyCost = energyCost;
		}

		public static ToolProps getFromId(int id) {
			return VALUES[id % VALUES.length];
		}
	}
	
	public enum ToolModes {
		NORMAL(TextFormatting.DARK_GREEN), BIG_HOLES(TextFormatting.LIGHT_PURPLE),
		VEIN(TextFormatting.AQUA), VEIN_EXTENDED(TextFormatting.RED);

		private static final ToolModes[] VALUES = values();
		public final String name;
		public final TextFormatting color;

		ToolModes(TextFormatting color) {
			this.name = EnumString.toolmode + "."
					+ CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
			this.color = color;
		}

		public static ToolModes getFromId(int id) {
			return VALUES[id % VALUES.length];
		}
	}

	protected ItemOmniTool() {
		setRegistryName(WantedOmniTool.MODID, "omnitool");
		setUnlocalizedName("omnitool");
		ModItems.toRegister.add(this);
		setCreativeTab(CreativeTabs.TOOLS);
	}
	
	public static ToolModes readToolMode(ItemStack stack) {
		return ToolModes.getFromId(MiscUtil.getOrCreateNbtData(stack).getInteger("toolMode"));
	}

	public static ToolModes readNextToolMode(ItemStack stack) {
		return ToolModes.getFromId(MiscUtil.getOrCreateNbtData(stack).getInteger("toolMode") + 1);
	}

	public static void saveToolMode(ItemStack stack, ToolModes mode) {
		MiscUtil.getOrCreateNbtData(stack).setInteger("toolMode", mode.ordinal());
	}

	public static ToolProps readToolProps(ItemStack stack) {
		NBTTagCompound tag = MiscUtil.getOrCreateNbtData(stack);
		return ToolProps.getFromId(tag.getInteger("toolProps"));
	}

	public static ToolProps readNextToolProps(ItemStack stack) {
		NBTTagCompound tag = MiscUtil.getOrCreateNbtData(stack);
		return ToolProps.getFromId(tag.getInteger("toolProps") + 1);
	}

	public static void saveToolProps(ItemStack stack, ToolProps props) {
		NBTTagCompound tag = MiscUtil.getOrCreateNbtData(stack);
		tag.setInteger("toolProps", props.ordinal());
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack) {
		return ImmutableSet.of("pickaxe", "spade", "sword", "axe", "shears", "scoop");
	}

	@Override
	public boolean canHarvestBlock(IBlockState blockIn) {
		return Items.DIAMOND_AXE.canHarvestBlock(blockIn) || Items.DIAMOND_PICKAXE.canHarvestBlock(blockIn)
				|| Items.DIAMOND_SHOVEL.canHarvestBlock(blockIn) || Items.SHEARS.canHarvestBlock(blockIn)
				|| Items.DIAMOND_SWORD.canHarvestBlock(blockIn);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		ToolProps props = readToolProps(stack);
		if (getEnergyStored(stack) >= props.energyCost) {
			return props.efficiency;
		}
		return 0.0F;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(MiscUtil.formatComplexMessage(TextFormatting.AQUA, EnumString.energy, TextFormatting.GREEN,
				getEnergyStored(stack) + " RF" + " / " + getMaxEnergyStored(stack) + " RF"));
		ToolModes mode = readToolMode(stack);
		ToolProps props = readToolProps(stack);
		tooltip.add(MiscUtil.formatComplexMessage(TextFormatting.BLUE, EnumString.toolmode, mode.color, mode.name));
		tooltip.add(MiscUtil.formatComplexMessage(TextFormatting.GOLD, EnumString.toolEfficiency, props.color, props.name));
	}

	/**
	 * Modes changer
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItemMainhand();
		if (!world.isRemote) {
			if (ClientProxy.modeKeyBinding.isKeyDown()) {
				ToolModes mode = readNextToolMode(stack);
				saveToolMode(stack, mode);
				player.sendMessage(new TextComponentString(MiscUtil.formatComplexMessage(TextFormatting.BLUE,
						EnumString.toolmode, mode.color, mode.name)));
			}

			if (ClientProxy.altKeyBinding.isKeyDown()) {
				ToolProps props = readNextToolProps(stack);
				saveToolProps(stack, props);
				player.sendMessage(new TextComponentString(MiscUtil.formatComplexMessage(TextFormatting.GOLD,
						EnumString.toolEfficiency, props.color, props.name)));
			}

			if (player.isSneaking()) {
				Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
				NBTTagList enchTagList = stack.getEnchantmentTagList();
				if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
					enchantmentMap.put(Enchantments.SILK_TOUCH, 1);
					player.sendMessage(new TextComponentString("Silk Enabled"));
				} else {
					enchantmentMap.put(Enchantments.FORTUNE, 3);
					player.sendMessage(new TextComponentString("Fortune Enabled"));
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

		}
		return super.onItemRightClick(world, player, hand);
	}

	/**
	 * Torch Placer - for all items
	 */
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!ClientProxy.modeKeyBinding.isKeyDown() &&
			!ClientProxy.altKeyBinding.isKeyDown()) {
			ItemStack torch = new ItemStack(Blocks.TORCH);
			IBlockState state = worldIn.getBlockState(pos);
			Block block = state.getBlock();
			ItemStack stack = ItemStack.EMPTY;
			if (!block.isReplaceable(worldIn, pos)) {
				pos = pos.offset(facing);
			}
			for (ItemStack stack1 : player.inventory.mainInventory) {
				if (stack1.getItem() == torch.getItem()) {
					stack = stack1;
					break;
				}
			}
			if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack)
					&& worldIn.mayPlace(Blocks.TORCH, pos, false, facing, player) && !player.isSneaking()) {
				IBlockState state1 = Blocks.TORCH.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0,
						player, hand);
				if (placeBlockAt(stack, player, worldIn, pos, state1)) {
					state1 = worldIn.getBlockState(pos);
					SoundType soundtype = state1.getBlock().getSoundType(state1, worldIn, pos, player);
					worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
							(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					if (!player.isCreative()) {
						stack.shrink(1);
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState) {
		if (!world.setBlockState(pos, newState, 11))
			return false;
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == Blocks.TORCH) {
			setTileEntityNBT(world, player, pos, stack);
			Blocks.TORCH.onBlockPlacedBy(world, pos, state, player, stack);
			if (player instanceof EntityPlayerMP) {
				CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
			}
		}
		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos originPos, EntityPlayer player) {
		if (!player.isSneaking()) {
			World world = player.world;
			ToolModes mode = readToolMode(stack);
			ToolProps props = readNextToolProps(stack);
			if (mode == ToolModes.BIG_HOLES) {
				for (BlockPos pos : getAoEBlocks(stack, player.world, player, originPos, true))
					if (getEnergyStored(stack) >= props.energyCost)
						ToolHelper.breakBlock(stack, player.world, player.world.getBlockState(pos), pos, player);
			}
			if (mode == ToolModes.VEIN || mode == ToolModes.VEIN_EXTENDED) {
				IBlockState state = world.getBlockState(originPos);
				Block block = state.getBlock();
				RayTraceResult rayTrace = doRayTrace(state, originPos, player);
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
				boolean veinGeneral = (mode == ToolModes.VEIN && isOre) || (mode == ToolModes.VEIN_EXTENDED);
				if (!player.capabilities.isCreativeMode && veinGeneral && !player.isSneaking()
						&& getEnergyStored(stack) >= props.energyCost) {
					for (BlockPos coord : findPositions(state, originPos, world)) {
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
								block2.onBlockDestroyedByPlayer(world, coord, state);
						} else {
							if (block2.removedByPlayer(state, world, coord, player, true)) {
								block2.onBlockDestroyedByPlayer(world, coord, state);
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

	private static List<BlockPos> findPositions(IBlockState state, BlockPos origin, World world) {
		List<BlockPos> found = new ArrayList<>();
		Set<BlockPos> checked = new ObjectOpenHashSet<>();
		found.add(origin);
		Block startBlock = state.getBlock();
		int maxCount = 120 - 1;
		for (int i = 0; i < found.size(); i++) {
			BlockPos blockPos = found.get(i);
			checked.add(blockPos);
			for (BlockPos pos : BlockPos.getAllInBox(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1))) {
				if (!checked.contains(pos)) {
					if (!world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
						if (startBlock == world.getBlockState(pos).getBlock()) {
							found.add(pos.toImmutable());
						}
						Block checkedBlock = world.getBlockState(pos).getBlock();
						if (startBlock == Blocks.REDSTONE_ORE || startBlock == Blocks.LIT_REDSTONE_ORE) {
							if (checkedBlock == Blocks.REDSTONE_ORE || checkedBlock == Blocks.LIT_REDSTONE_ORE) {
								found.add(pos.toImmutable());
							}
						}
						if (found.size() > maxCount) {
							return found;
						}
					}
				}
			}
		}
		return found;
	}

	public ImmutableList<BlockPos> getAoEBlocks(ItemStack stack, World world, EntityPlayer player, BlockPos pos,
			boolean harvest) {
		RayTraceResult mop = rayTrace(world, player, false);
		return getAoEBlocks(world, player, pos, mop.sideHit, harvest);
	}

	private RayTraceResult doRayTrace(IBlockState state, BlockPos pos, EntityPlayer player) {
		Vec3d positionEyes = player.getPositionEyes(1.0F);
		Vec3d playerLook = player.getLook(1.0F);
		double blockReachDistance = player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE)
				.getAttributeValue();
		Vec3d maxReach = positionEyes.addVector(playerLook.x * blockReachDistance, playerLook.y * blockReachDistance,
				playerLook.z * blockReachDistance);
		RayTraceResult res = state.collisionRayTrace(player.world, pos, playerLook, maxReach);
		return res != null ? res : new RayTraceResult(RayTraceResult.Type.MISS, Vec3d.ZERO, EnumFacing.UP, pos);
	}

	public ImmutableList<BlockPos> getAoEBlocks(World world, EntityPlayer player, BlockPos pos, EnumFacing side,
			boolean harvest) {
		if (harvest && !ForgeHooks.canHarvestBlock(world.getBlockState(pos).getBlock(), player, world, pos))
			return ImmutableList.of();
		int xRadius = 1, yRadius = 1, zRadius = 1;
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

	public int getEnergyUsage(ItemStack stack) {
		return readToolProps(stack).energyCost;
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
		drainEnergy(stack, readToolProps(stack).energyCost, target);
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
		}
		return 0;
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
}