package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.client.model.hatch.HatchBakedModel;
import es.degrassi.mmreborn.common.block.prop.ItemDurabilityHatchSize;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.DurabilityComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateItemComponentPacket;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

@Getter
public class DurabilityHatchEntity extends TileInventory implements MachineComponentEntity<DurabilityComponent>, ControllerAccessible, TextureableMachineEntity {
  private BlockPos controllerPos;
  private ItemDurabilityHatchSize size;

  @Getter
  @Setter
  private ResourceLocation baseTexture;
  @Getter
  @Setter
  private ResourceLocation overlayTexture;
  @Getter
  private ResourceLocation defaultOverlayTexture;
  @Getter
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");

  private DurabilityHatchEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState,
                          ItemDurabilityHatchSize size) {
    super(entityType, pos, blockState, size.getSlotCount());
    this.size = size;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_durabilityhatch_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.inventory.setListener(new IOInventory.IOInventoryChangedListener() {
      @Override
      public void onChange(int slot, ItemStack stack) {
        if (getController() != null)
          getController().getProcessor().setMachineInventoryChanged();
        if (getLevel() instanceof ServerLevel l)
          PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()),
              new SUpdateItemComponentPacket(slot, stack, getBlockPos()));
      }

      @Override
      public void onChange() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
          onChange(slot, inventory.getStackInSlot(slot));
        }
      }
    });
  }

  public DurabilityHatchEntity(BlockPos pos, BlockState blockState, ItemDurabilityHatchSize size) {
    this(EntityRegistration.ITEM_DURABILITY_HATCH.get(), pos, blockState, size);
  }
  public DurabilityHatchEntity(BlockPos pos, BlockState blockState) {
    this(EntityRegistration.ITEM_DURABILITY_HATCH.get(), pos, blockState, ItemDurabilityHatchSize.TINY);
  }

  @Override
  public IOInventory buildInventory(int slots) {
    int[] inSlots = new int[slots];
    for (int i = 0; i < slots; i++) {
      inSlots[i] = i;
    }
    return new IOInventory(inSlots, new int[0], stack -> stack.has(DataComponents.DAMAGE), Direction.values());
  }

  @Nullable
  @Override
  public DurabilityComponent provideComponent() {
    return new DurabilityComponent(this.getInventory());
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.size = ItemDurabilityHatchSize.value(compound.getString("busSize"));
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }

    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_durabilityhatch_" + size.getSerializedName());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;

    this.inventory.setListener(new IOInventory.IOInventoryChangedListener() {
      @Override
      public void onChange(int slot, ItemStack stack) {
        if (getController() != null)
          getController().getProcessor().setMachineInventoryChanged();
        if (getLevel() instanceof ServerLevel l)
          PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()),
              new SUpdateItemComponentPacket(slot, stack, getBlockPos()));
      }

      @Override
      public void onChange() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
          onChange(slot, inventory.getStackInSlot(slot));
        }
      }
    });
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);

    compound.putString("busSize", this.size.getSerializedName());
    if (controllerPos != null)
      compound.putLong("controllerPos", controllerPos.asLong());
    if (baseTexture != null)
      compound.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      compound.putString("overlayTexture", overlayTexture.toString());
  }

  @Override
  public void setControllerPos(BlockPos pos) {
    this.controllerPos = pos;
  }

  @Override
  public ModelData getModelData() {
    ModelData.Builder builder = getModelDataBuilder("all");
    builder.with(HatchBakedModel.BASE_TEXTURE, baseTexture)
        .with(HatchBakedModel.BASE_TEXTURE_NAME, "bg_all");
    builder.with(HatchBakedModel.OVERLAY_TEXTURE, overlayTexture)
        .with(HatchBakedModel.OVERLAY_TEXTURE_NAME, "ov_all");
    return builder.build();
  }

  @Override
  public ResourceLocation getMachineBaseTexture() {
    return baseTexture;
  }

  @Override
  public ResourceLocation getMachineOverlayTexture() {
    return overlayTexture;
  }

  @Override
  public void setMachineBaseTexture(ResourceLocation newTexture) {
    setChanged();
    this.baseTexture = newTexture;
    setRequestModelUpdate(true);
    triggerEvent(1, 0);
    this.markForUpdate();
    if (getLevel() instanceof ServerLevel l) {
      PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()),
          new SUpdateMachineTexturePacket(baseTexture, true, getBlockPos()));
    }
  }

  @Override
  public void setMachineOverlayTexture(ResourceLocation newTexture) {
    setChanged();
    this.overlayTexture = newTexture;
    setRequestModelUpdate(true);
    triggerEvent(1, 0);
    this.markForUpdate();
    if (getLevel() instanceof ServerLevel l) {
      PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()),
          new SUpdateMachineTexturePacket(overlayTexture, false, getBlockPos()));
    }
  }

  public void resetTextures() {
    setMachineBaseTexture(defaultBaseTexture);
    setMachineOverlayTexture(defaultOverlayTexture);
  }

  @Override
  public MachineHatchType getHatchType() {
    return (switch (size) {
      case TINY -> MachineHatchTypeRegistration.DURABILITY_HATCH_TINY;
      case SMALL -> MachineHatchTypeRegistration.DURABILITY_HATCH_SMALL;
      case NORMAL -> MachineHatchTypeRegistration.DURABILITY_HATCH_NORMAL;
      case BIG -> MachineHatchTypeRegistration.DURABILITY_HATCH_BIG;
    }).get();
  }
}
