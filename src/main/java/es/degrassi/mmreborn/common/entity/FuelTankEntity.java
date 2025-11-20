package es.degrassi.mmreborn.common.entity;

import com.google.common.collect.Maps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.capability.BasicFuelHandler;
import es.degrassi.mmreborn.api.capability.IFuelHandler;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.BooleanSyncable;
import es.degrassi.mmreborn.api.network.syncable.LongSyncable;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.common.block.prop.FuelTankSize;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.data.config.FuelTankConfig;
import es.degrassi.mmreborn.common.entity.base.IAutoEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.entity.base.IServerTickEntity;
import es.degrassi.mmreborn.common.entity.base.ITickEntity;
import es.degrassi.mmreborn.common.entity.base.MachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.entity.base.TileInventory;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.FuelComponent;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateFuelComponentPacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateItemComponentPacket;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.ItemSlot;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Setter
@MethodsReturnNonnullByDefault
public class FuelTankEntity extends TileInventory implements MachineComponentEntity<FuelComponent>,
    ControllerAccessible, TextureableMachineEntity, ITickEntity, IServerTickEntity, ISyncableStuff, IAutoInputEntity,
    IAutoEntity<IItemHandler> {
  @Nullable
  private BlockPos controllerPos;
  private FuelTankSize size;

  private ResourceLocation baseTexture;
  private ResourceLocation overlayTexture;
  private ResourceLocation defaultOverlayTexture;
  @Getter
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");
  private final IFuelHandler fuelHandler;
  private final long tickOffset = Utils.RAND.nextIntBetweenInclusive(0, Integer.MAX_VALUE - 1);
  private long lastCheckTick, lastCheckFuelTick;
  private final Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

  private FuelTankEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, FuelTankSize size) {
    super(type, pos, state, 1);
    this.size = size;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_fueltank_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.baseTexture = defaultBaseTexture;
    this.inventory.setListener(new IOInventory.IOInventoryChangedListener() {
      @Override
      public void onChange(int slot, @NotNull ItemStack stack) {
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
    this.fuelHandler = new BasicFuelHandler(this.getInventory());
    this.fuelHandler.setMaxFuel(size.burnTimeCapacity);
    this.fuelHandler.setListener(() -> {
      if (getLevel() != null && !getLevel().isClientSide)
        PacketDistributor.sendToPlayersTrackingChunk(
            (ServerLevel) getLevel(),
            new ChunkPos(getBlockPos()),
            new SUpdateFuelComponentPacket(fuelHandler.getFuel(), fuelHandler.getMaxFuel(), getBlockPos())
        );
      if (getController() != null)
        getController().getProcessor().setMachineInventoryChanged();
    });
    this.shouldAutoInput = true;
  }

  public FuelTankEntity(BlockPos pos, BlockState state, FuelTankSize size) {
    this(EntityRegistration.FUEL_TANK.get(), pos, state, size);
  }
  public FuelTankEntity(BlockPos pos, BlockState state){
    this(pos, state, FuelTankSize.TINY);
  }

  @Override
  public void doRestrictedTick() {
    IServerTickEntity.super.doRestrictedTick();
    long gameTime = getLevel().getGameTime();
    if (FuelTankConfig.get().reduceFuelPerTick.get()) {
      if (Utils.shouldRunPeriodicCheck(false, gameTime, lastCheckFuelTick, tickOffset, 1))
        if (getFuelHandler().getFuel() > 0 && (getController() == null || getController().getStatus() != MachineStatus.RUNNING)) {
          getFuelHandler().removeFuel(1);
        }
      lastCheckFuelTick = gameTime;
    }
    if (!Utils.shouldRunPeriodicCheck(false, gameTime, lastCheckTick, tickOffset, MMRConfig.get().checkRecipeTicks.get()))
      return;
    lastCheckTick = gameTime;
    getFuelHandler().tryBurnItem();
  }

  @Override
  public IOInventory buildInventory(int slots) {
    int[] inSlots = new int[slots];
    for (int i = 0; i < slots; i++) {
      inSlots[i] = i;
    }
    return new IOInventory(inSlots, new int[0], stack -> stack.getBurnTime(RecipeType.SMELTING) > 0, Direction.values());
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.getFuelHandler().deserialize(compound.getCompound("fuelTank"), pRegistries);
    this.size = FuelTankSize.value(compound.getString("busSize"));
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_fueltank_" + size.getSerializedName());
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
    CompoundTag fuelTag = new CompoundTag();
    this.getFuelHandler().serialize(fuelTag, pRegistries);
    compound.put("fuelTank", fuelTag);

    compound.putString("busSize", this.size.getSerializedName());
    if (controllerPos != null)
      compound.putLong("controllerPos", controllerPos.asLong());
    if (baseTexture != null)
      compound.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      compound.putString("overlayTexture", overlayTexture.toString());
  }
  @Override
  public ModelData getModelData() {
    return getModelDataBuilder("all").build();
  }

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
    return (switch (size) {
      case TINY -> MachineHatchTypeRegistration.FUEL_TANK_TINY;
      case SMALL -> MachineHatchTypeRegistration.FUEL_TANK_SMALL;
      case NORMAL -> MachineHatchTypeRegistration.FUEL_TANK_NORMAL;
      case REINFORCED -> MachineHatchTypeRegistration.FUEL_TANK_REINFORCED;
      case BIG -> MachineHatchTypeRegistration.FUEL_TANK_BIG;
      case HUGE -> MachineHatchTypeRegistration.FUEL_TANK_HUGE;
    }).get();
  }

  @Override
  public FuelComponent provideComponent() {
    return new FuelComponent(getFuelHandler());
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(LongSyncable.create(getFuelHandler()::getFuel, getFuelHandler()::setFuel));
    container.accept(LongSyncable.create(getFuelHandler()::getMaxFuel, getFuelHandler()::setMaxFuel));
    container.accept(BooleanSyncable.create(this::isShouldAutoInput, this::setShouldAutoInput));
  }

  @Override
  public void tickAutoInput() {
    if (!shouldAutoInput) return;
    for (Direction side : Direction.values()) {
      var neighbour = getNeighbour(Capabilities.ItemHandler.BLOCK, side);
      if (neighbour == null) continue;

      inventory.getInventory()
          .stream()
          .filter(ItemSlot::isInput)
          .forEachOrdered(slot -> moveStacks(neighbour, slot, Integer.MAX_VALUE));
    }
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
