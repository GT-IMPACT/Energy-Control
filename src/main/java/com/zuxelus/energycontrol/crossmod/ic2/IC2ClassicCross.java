package com.zuxelus.energycontrol.crossmod.ic2;

import java.util.ArrayList;
import java.util.List;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.ItemStackHelper;
import com.zuxelus.energycontrol.items.ItemHelper;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import com.zuxelus.energycontrol.utils.ReactorHelper;

import ic2.api.classic.reactor.IChamberReactor;
import ic2.api.item.IC2Items;
import ic2.api.item.ICustomDamageItem;
import ic2.api.reactor.IReactor;
import ic2.api.tile.IEnergyStorage;
import ic2.core.block.generator.tile.TileEntityLiquidFuelGenerator;
import ic2.core.block.generator.tile.TileEntityNuclearReactorElectric;
import ic2.core.block.generator.tile.TileEntityNuclearSteamReactor;
import ic2.core.block.generator.tile.TileEntityReactorChamberElectric;
import ic2.core.block.machine.low.TileEntityMachineTank;
import ic2.core.block.personal.tile.TileEntityPersonalTank;
import ic2.core.item.reactor.ItemReactorUraniumRod;
import ic2.core.item.tool.ItemToolWrench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;

public class IC2ClassicCross extends IC2Cross {
	@Override
	public int getNuclearCellTimeLeft(ItemStack stack) {
		if (stack.isEmpty())
			return 0;
		
		Item item = stack.getItem();
		if (item instanceof ItemReactorUraniumRod)
			return ((ICustomDamageItem)item).getMaxCustomDamage(stack) - ((ICustomDamageItem)item).getCustomDamage(stack);
		
		return 0;
	}

	@Override
	public boolean isSteamReactor(TileEntity par1) {
		return par1 != null && par1 instanceof IReactor;
	}

	@Override
	public IC2Type getType() {
		return IC2Type.SPEIGER;
	}

	@Override
	public int getProfile() {
		return -1;
	}

	@Override
	public ItemStack getItem(String name) {
		switch (name) {
		case "transformer":
			return IC2Items.getItem("upgrade", "transformer");
		case "energy_storage":
			return IC2Items.getItem("upgrade", "energy_storage");
		case "machine":
			return IC2Items.getItem("resource", "machine");
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isWrench(ItemStack par1) {
		return par1 != null && par1.getItem() instanceof ItemToolWrench;
	}

	@Override
	public boolean isCable(TileEntity te) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ItemStack getGeneratorCard(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		return ItemStack.EMPTY;
	}

	@Override
	public NBTTagCompound getGeneratorData(TileEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NBTTagCompound getGeneratorKineticData(TileEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NBTTagCompound getGeneratorHeatData(TileEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IFluidTank> getAllTanks(TileEntity te) {
		List<IFluidTank> result = new ArrayList<>();
		if (te instanceof TileEntityNuclearSteamReactor) {
			result.add(((TileEntityNuclearSteamReactor) te).getWaterTank());
			result.add(((TileEntityNuclearSteamReactor) te).getSteamTank());
		}
		if (te instanceof TileEntityMachineTank)
			result.add(((TileEntityMachineTank) te).tank);
		if (te instanceof TileEntityPersonalTank)
			result.add(((TileEntityPersonalTank) te).tank);
		if (te instanceof TileEntityLiquidFuelGenerator)
			result.add(((TileEntityLiquidFuelGenerator) te).tank);
		if (result.size() == 0)
			return null;
		return result;
	}

	@Override
	public ItemStack getReactorCard(World world, BlockPos pos) {
		/*Block block = world.getBlockState(pos).getBlock();
		if (!(block instanceof BlockTileEntity))
			return ItemStack.EMPTY;*/

		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNuclearReactorElectric || te instanceof TileEntityReactorChamberElectric || te instanceof TileEntityNuclearSteamReactor) {
			BlockPos position = ReactorHelper.getTargetCoordinates(world, pos);
			if (position != null) {
				ItemStack sensorLocationCard = new ItemStack(ItemHelper.itemCard, 1, ItemCardType.CARD_REACTOR);
				ItemStackHelper.setCoordinates(sensorLocationCard, position);
				return sensorLocationCard;
			}
		}/* else if (te instanceof TileEntityReactorFluidPort || te instanceof TileEntityReactorRedstonePort
				|| te instanceof TileEntityReactorAccessHatch) {
			BlockPos position = ReactorHelper.get5x5TargetCoordinates(world, pos);
			if (position != null) {
				ItemStack sensorLocationCard = new ItemStack(ItemHelper.itemCard, 1, ItemCardType.CARD_REACTOR5X5);
				ItemStackHelper.setCoordinates(sensorLocationCard, position);
				return sensorLocationCard;
			}
		}*/
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getLiquidAdvancedCard(World world, BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CardState updateCardReactor(World world, ICardReader reader, IReactor reactor) {
		if (!(reactor instanceof IChamberReactor))
			return CardState.NO_TARGET;
	
		reader.setInt("heat", reactor.getHeat());
		reader.setInt("maxHeat", reactor.getMaxHeat());
		reader.setBoolean("reactorPoweredB", reactor.produceEnergy());
		reader.setInt("output", (int) Math.round(reactor.getReactorEUEnergyOutput()));
		boolean isSteam = ReactorHelper.isSteam(reactor);
		reader.setBoolean("isSteam", isSteam);

		IChamberReactor chamber = (IChamberReactor) reactor;
		int size = chamber.getReactorSize();
		int dmgLeft = 0;
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < size; x++) {
				ItemStack stack = chamber.getItemAt(x, y);
				if (!stack.isEmpty())
					dmgLeft = Math.max(dmgLeft, ReactorHelper.getNuclearCellTimeLeft(stack));
			}
		}

		int timeLeft = 0;
		//Classic has a Higher Tick rate for Steam generation but damage tick rate is still the same...
		if (isSteam) {
			timeLeft = dmgLeft;
		} else
			timeLeft = dmgLeft * reactor.getTickRate() / 20;
		reader.setInt("timeLeft", timeLeft);
		return CardState.OK;
	}

	@Override
	public CardState updateCardReactor5x5(World world, ICardReader reader, BlockPos target) {
		// TODO Auto-generated method stub
		return CardState.NO_TARGET;
	}

	@Override
	public ItemStack getEnergyCard(World world, BlockPos pos) {
		// TODO Auto-generated method stub
		return ItemStack.EMPTY;
	}

	@Override
	public NBTTagCompound getEnergyData(TileEntity te) {
		// TODO Auto-generated method stub
		return null;
	}
}