package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.common.util.IEntitySynchronizable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@Setter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockEntitySynchronized extends BlockEntity implements IEntitySynchronizable {
  private static final String REQUEST_UPDATE_KEY = "requestModelUpdate";
  private static final String IN_STRUCTURE_KEY = "inStructure";
  private boolean requestModelUpdate = false;
  private boolean inStructure = false;

  public BlockEntitySynchronized(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
    super(type, pos, blockState);
  }

  public void tick() {
    // Used by overrides with no functionality by default
  }

  @Override
  protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
    super.loadAdditional(nbt, pRegistries);
    requestModelUpdate = nbt.contains(REQUEST_UPDATE_KEY) && nbt.getBoolean(REQUEST_UPDATE_KEY);
    inStructure = nbt.contains(IN_STRUCTURE_KEY) && nbt.getBoolean(IN_STRUCTURE_KEY);
  }

  @Override
  protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
    super.saveAdditional(nbt, pRegistries);
    nbt.putBoolean(REQUEST_UPDATE_KEY, requestModelUpdate);
    nbt.putBoolean(IN_STRUCTURE_KEY, inStructure);
  }

  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
    CompoundTag nbt = super.getUpdateTag(pRegistries);
    saveAdditional(nbt, pRegistries);
    return nbt;
  }

  @Override
  public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
    loadAdditional(tag, lookupProvider);
  }

  @Override
  public final ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  public void markForUpdate() {
    if (requestModelUpdate) requestModelDataUpdate();
    setRequestModelUpdate(false);
    getLevel().setBlockAndUpdate(getBlockPos(), getBlockState());
    setChanged();
  }
}
