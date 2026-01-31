package es.degrassi.mmreborn.common.entity.base;

import com.google.common.collect.Maps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.capability.config.IOSideConfig;
import es.degrassi.mmreborn.api.capability.config.IOSideMode;
import es.degrassi.mmreborn.api.capability.config.ISideConfigComponent;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.IOSideConfigSyncable;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.common.block.prop.ItemBusSize;
import es.degrassi.mmreborn.common.entity.ItemInputBusEntity;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.manager.handler.AbstractHandler;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateItemComponentPacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class TileItemBus extends TileInventory implements MachineComponentEntity<ItemComponent>, ControllerAccessible, TextureableMachineEntity, ITickEntity,
    IServerTickEntity, ISyncableStuff, IAutoEntity<IItemHandler>, ISideConfigComponent<IOSideMode> {
  @Nullable private BlockPos controllerPos;
  private ItemBusSize size;
  private IOType ioType;

  private ResourceLocation baseTexture;
  private ResourceLocation overlayTexture;
  private ResourceLocation defaultOverlayTexture;
  @Getter
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");
  private final Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

  private final IOSideConfig config;

  protected TileItemBus(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState, ItemBusSize size, IOType ioType) {
    super(entityType, pos, blockState, size.getSlotCount(), size.stackSize);
    this.size = size;
    this.ioType = ioType;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_" + ioType.getSerializedName() + "bus_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.inventory.setListener(new AbstractHandler.HandlerUpdateListener<>() {
      @Override
      public void onChange(int slot, ItemStack stack) {
        getControllerPosSet().forEach(p -> {
          if (getLevel() == null) return;
          if (getLevel().isClientSide()) return;
          if (getLevel().getBlockEntity(p) instanceof MachineControllerEntity controller) {
            controller.getProcessor().setMachineInventoryChanged();
          }
        });
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
    this.config = IOSideConfig.Template.DEFAULT_ALL_DISABLED.build(this);
    this.config.setCallback(this::configChanged);
  }

  @Nullable
  @Override
  public ItemComponent provideComponent() {
    return new ItemComponent(this.getInventory(), ioType);
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.size = ItemBusSize.value(compound.getString("busSize"));
    this.ioType = IOType.getByString(compound.getString("ioType"));
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }

    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_" + ioType.getSerializedName() + "bus_" + size.getSerializedName());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;

    this.inventory.setListener(new AbstractHandler.HandlerUpdateListener<>() {
      @Override
      public void onChange(int slot, ItemStack stack) {
        getControllerPosSet().forEach(p -> {
          if (getLevel() == null) return;
          if (getLevel().isClientSide()) return;
          if (getLevel().getBlockEntity(p) instanceof MachineControllerEntity controller) {
            controller.getProcessor().setMachineInventoryChanged();
          }
        });
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

    this.config.deserialize(compound.getCompound("config"));
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);

    compound.putString("busSize", this.size.getSerializedName());
    if (ioType == null) {
      ioType = this instanceof ItemInputBusEntity ? IOType.INPUT : IOType.OUTPUT;
    }
    compound.putString("ioType", this.ioType.getSerializedName());
    if (controllerPos != null)
      compound.putLong("controllerPos", controllerPos.asLong());
    if (baseTexture != null)
      compound.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      compound.putString("overlayTexture", overlayTexture.toString());
    compound.put("config", this.config.serialize());
  }

  @Override
  public void setControllerPos(BlockPos pos) {
    this.controllerPos = pos;
  }

  @Override
  public ModelData getModelData() {
    return getModelDataBuilder("all").build();
  }

  @Override
  public HatchTextureData getTextureData(String mode) {
    return MachineComponentEntity.super.getTextureData(mode).derive(
        "bg_all",
        baseTexture,
        defaultBaseTexture,
        "ov_all",
        overlayTexture,
        defaultOverlayTexture,
        false
    );
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
    return switch(ioType) {
      case INPUT -> (switch (size) {
        case TINY -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_TINY;
        case SMALL -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_BIG;
        case HUGE -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.ITEM_INPUT_BUS_LUDICROUS;
      }).get();
      case OUTPUT -> (switch(size) {
        case TINY -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_TINY;
        case SMALL -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_BIG;
        case HUGE -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.ITEM_OUTPUT_BUS_LUDICROUS;
      }).get();
      default -> null;
    };
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(IOSideConfigSyncable.create(this::getConfig, this.config::set));
  }

  protected void moveStacks(IItemHandler from, IItemHandler to, int maxAmount) {
    for (int i = 0; i < from.getSlots(); i++) {
      ItemStack canExtract = from.extractItem(i, maxAmount, true);
      if (canExtract.isEmpty()) continue;
      ItemStack canInsert = ItemHandlerHelper.insertItemStacked(to, canExtract, false);
      if (canInsert.isEmpty()) {
        from.extractItem(i, maxAmount, false);
      } else{
        from.extractItem(i, canExtract.getCount() - canInsert.getCount(), false);
      }
    }
  }
}
