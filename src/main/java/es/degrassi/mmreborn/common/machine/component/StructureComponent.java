package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.entity.StructureCheckerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class StructureComponent extends MachineComponent<Structure> {
  private final StructureCheckerEntity entity;
  public StructureComponent(StructureCheckerEntity entity) {
    super(IOType.INPUT);
    this.entity = entity;
  }

  @Override
  public ComponentType<Structure> getComponentType() {
    return ComponentRegistration.COMPONENT_STRUCTURE.get();
  }

  @Override
  public @Nullable Structure getContainerProvider() {
    return null;
  }

  @Override
  public <C extends MachineComponent<Structure>> boolean canMerge(C c) {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<Structure>> C merge(C c) {
    return (C) this;
  }

  public void destroyStructure(Structure structure, boolean drops) {
    structure.getBlocks(entity.getControllerFacing()).forEach((pos, ingredients) -> {
      if (ingredients != BlockIngredient.STRUCTURE_CHECKER && ingredients != BlockIngredient.ANY)
        entity.getLevel().destroyBlock(pos.offset(entity.getBlockPos()), drops);
    });
  }

  public boolean checkStructure(Structure structure) {
    return structure.match(entity.getLevel(), entity.getBlockPos(), entity.getControllerFacing());
  }

  public void placeStructure(Structure structure, boolean drops) {
    structure.getBlocks(entity.getControllerFacing()).forEach((pos, ingredients) -> {
      BlockPos worldPos = pos.offset(entity.getBlockPos());
      if(pos != BlockPos.ZERO && ingredients != BlockIngredient.ANY) {
        if(!entity.getLevel().getBlockState(worldPos).isAir())
          entity.getLevel().destroyBlock(worldPos, drops);
        setBlock(
            entity.getLevel(),
            worldPos,
            ingredients.isNot()
                ? PartialBlockState.AIR
                : ingredients.getAll().stream().findAny().orElse(PartialBlockState.AIR)
        );
      }
    });
  }

  private void setBlock(Level world, BlockPos pos, PartialBlockState state) {
    world.setBlockAndUpdate(pos, state.getBlockState());
    BlockEntity tile = world.getBlockEntity(pos);
    if(tile != null && state.getNbt() != null && !state.getNbt().isEmpty()) {
      CompoundTag nbt = state.getNbt().copy();
      nbt.putInt("x", pos.getX());
      nbt.putInt("y", pos.getY());
      nbt.putInt("z", pos.getZ());
      tile.loadWithComponents(nbt, world.registryAccess());
    }
  }
}
