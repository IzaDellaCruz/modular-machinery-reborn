package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.common.util.IOInventory;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Getter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TileInventory extends ColorableMachineComponentEntity implements ItemDroppeable {
  protected final IOInventory inventory;
  private final int slots;

  protected TileInventory(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState, int slots) {
    super(entityType, pos, blockState);
    this.inventory = buildInventory(slots);
    this.slots = slots;
  }

  protected TileInventory(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState, int slots, int stackSize) {
    super(entityType, pos, blockState);
    this.inventory = buildInventory(slots, stackSize);
    this.slots = slots;
  }

  public IOInventory buildInventory(int slots) {
    return buildInventory(slots, 64);
  }

  public abstract IOInventory buildInventory(int slots, int slotLimit);

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.inventory.deserialize(compound.getCompound("inventory"), pRegistries);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);
    compound.put("inventory", this.inventory.writeNBT(pRegistries));
  }

  @Override
  public void addDrops(List<ItemStack> drops) {
    this.inventory.getAllStacks().forEach(stack -> drops.add(stack.copy()));
  }
}
