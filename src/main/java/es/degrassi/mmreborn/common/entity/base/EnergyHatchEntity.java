package es.degrassi.mmreborn.common.entity.base;

import com.google.common.collect.Maps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.BooleanSyncable;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.common.block.prop.EnergyHatchSize;
import es.degrassi.mmreborn.common.entity.EnergyInputHatchEntity;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateEnergyComponentPacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.IEnergyHandler;
import es.degrassi.mmreborn.common.util.MiscUtils;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class EnergyHatchEntity extends ColorableMachineComponentEntity implements IEnergyHandler,
    MachineComponentEntity<EnergyComponent>, ControllerAccessible, TextureableMachineEntity, CapabilityInventoryEntity<IEnergyStorage>, ITickEntity, IServerTickEntity,
    ISyncableStuff, IAutoEntity<IEnergyStorage> {

  protected long energy = 0;
  protected EnergyHatchSize size;
  protected IOType ioType;
  @Getter
  private BlockPos controllerPos;

  private boolean canExtract = false;
  private boolean canInsert = false;

  @Getter
  @Setter
  private ResourceLocation baseTexture;
  @Getter
  @Setter
  private ResourceLocation overlayTexture;
  @Getter
  private ResourceLocation defaultOverlayTexture;

  @Getter
  private final ItemHandler capabilityInventory;

  private final long tickOffset = Utils.RAND.nextIntBetweenInclusive(0, Integer.MAX_VALUE - 1);
  private long lastCheckTick;

  @Getter
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");

  @Getter
  private final Map<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

  protected EnergyHatchEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, EnergyHatchSize size,
                          IOType ioType) {
    super(type, pos, state);
    this.size = size;
    this.ioType = ioType;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_energy" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.capabilityInventory = createCapabilityInventory();
    this.shouldAutoOutput = ioType.isOutput();
    this.shouldAutoInput = ioType.isInput();
  }

  @Override
  public ItemCapability<IEnergyStorage, Void> getCapability() {
    return Capabilities.EnergyStorage.ITEM;
  }

  @Override
  public IOType getMode() {
    return ioType;
  }

  @Override
  public void doRestrictedTick() {
    IServerTickEntity.super.doRestrictedTick();
    tickInventory();
  }

  public boolean shouldTickInventory() {
    long gameTime = getLevel().getGameTime();
    if (!Utils.shouldRunPeriodicCheck(false, gameTime, lastCheckTick, tickOffset, 2))
      return false;
    lastCheckTick = gameTime;
    return true;
  }

  @Override
  public void tickInventory() {
    if (!shouldTickInventory()) return;
    capabilityInventory.getInventory().forEach(slot -> {
      Optional.ofNullable(slot.getItemStack().getCapability(getCapability())).ifPresent(cap -> {
        if (ioType == IOType.NONE) return;
        if (ioType.isInput()) {
          if (!cap.canExtract()) return;
          if (!this.canReceive()) return;
          if (this.getCurrentEnergy() >= this.getMaxEnergy()) return;
          int simulatedCap = cap.extractEnergy(Integer.MAX_VALUE, true);
          int simulatedInsert = receiveEnergy(simulatedCap, true);
          cap.extractEnergy(simulatedInsert, false);
          receiveEnergy(simulatedInsert, false);
        } else if (ioType.isOutput()) {
          if (!cap.canReceive()) return;
          if (!this.canExtract()) return;
          if (this.getEnergyStored() == 0) return;
          int simulatedExtract = extractEnergy(Integer.MAX_VALUE, true);
          int simulatedCap = cap.receiveEnergy(simulatedExtract, true);
          cap.receiveEnergy(simulatedCap, false);
          extractEnergy(simulatedCap, false);
        }
      });
    });
  }

  @Nullable
  @Override
  public EnergyComponent provideComponent() {
    return new EnergyComponent(this, ioType);
  }

  @Override
  public void setCanExtract(boolean canExtract) {
    this.canExtract = canExtract;
  }

  @Override
  public void setCanInsert(boolean canInsert) {
    this.canInsert = canInsert;
  }

  private void onContentsChange() {
    getControllerPosSet().forEach(p -> {
      if (getLevel() == null) return;
      if (getLevel().isClientSide()) return;
      if (getLevel().getBlockEntity(p) instanceof MachineControllerEntity controller) {
        controller.getProcessor().setMachineInventoryChanged();
      }
    });
  }

  @Override
  public int receiveEnergy(int maxReceive, boolean simulate) {
    if (!canReceive()) {
      return 0;
    }
    int insertable = this.energy + maxReceive > this.size.maxEnergy ? convertDownEnergy(this.size.maxEnergy - this.energy) : maxReceive;
    insertable = Math.min(insertable, convertDownEnergy(size.transferLimit));
    if (!simulate) {
      this.energy = MiscUtils.clamp(this.energy + insertable, 0, this.size.maxEnergy);
      markForUpdate();
      onContentsChange();
      if (getLevel() instanceof ServerLevel l)
        PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()), new SUpdateEnergyComponentPacket(this.energy, getBlockPos()));
    }
    return insertable;
  }

  @Override
  public int extractEnergy(int maxExtract, boolean simulate) {
    if (!canExtract()) {
      return 0;
    }
    int extractable = this.energy - maxExtract < 0 ? convertDownEnergy(this.energy) : maxExtract;
    extractable = Math.min(extractable, convertDownEnergy(size.transferLimit));
    if (!simulate) {
      this.energy = MiscUtils.clamp(this.energy - extractable, 0, this.size.maxEnergy);
      onContentsChange();
      markForUpdate();
      if (getLevel() instanceof ServerLevel l)
        PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()), new SUpdateEnergyComponentPacket(this.energy, getBlockPos()));
    }
    return extractable;
  }

  @Override
  public int getEnergyStored() {
    return convertDownEnergy(this.energy);
  }

  @Override
  public int getMaxEnergyStored() {
    return convertDownEnergy(this.size.maxEnergy);
  }

  @Override
  public boolean canExtract() {
    return canExtract || ioType != null && !ioType.isInput();
  }

  @Override
  public boolean canReceive() {
    return canInsert || ioType != null && ioType.isInput();
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.energy = compound.getLong("energy");
    this.ioType = IOType.getByString(compound.getString("ioType"));
    this.size = EnergyHatchSize.value(compound.getString("hatchSize").toUpperCase(Locale.ROOT));
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }
    onContentsChange();
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_energy" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;
    this.capabilityInventory.deserialize(compound.getCompound("inventory"), pRegistries);
    this.shouldAutoOutput = this.ioType.isOutput() && this.shouldAutoOutput;
    this.shouldAutoInput = ioType.isInput() && shouldAutoInput;
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);

    compound.putLong("energy", this.energy);
    if (ioType == null) {
      ioType = this instanceof EnergyInputHatchEntity ? IOType.INPUT : IOType.OUTPUT;
    }
    compound.putString("ioType", ioType.getSerializedName());
    compound.putString("hatchSize", this.size.getSerializedName());
    if (controllerPos != null)
      compound.putLong("controllerPos", controllerPos.asLong());
    if (baseTexture != null)
      compound.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      compound.putString("overlayTexture", overlayTexture.toString());
    compound.put("inventory", this.capabilityInventory.writeNBT(pRegistries));
  }

  @Override
  public void setControllerPos(BlockPos pos) {
    this.controllerPos = pos;
  }

  protected int convertDownEnergy(long energy) {
    return energy >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
  }

  public EnergyHatchSize getTier() {
    return size;
  }

  @Override
  public long getCurrentEnergy() {
    return this.energy;
  }

  @Override
  public void setCurrentEnergy(long energy) {
    this.energy = MiscUtils.clamp(energy, 0, getMaxEnergy());

    if (getLevel() instanceof ServerLevel l)
      PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()), new SUpdateEnergyComponentPacket(this.energy, getBlockPos()));
    onContentsChange();
    markForUpdate();
  }

  @Override
  public long getMaxEnergy() {
    return this.size.maxEnergy;
  }

  @Override
  public ModelData getModelData() {
    return getModelDataBuilder("all").build();
  }

  @Override
  public HatchTextureData getTextureData(@NotNull String mode) {
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

  @Override
  public MachineHatchType getHatchType() {
    return switch(ioType) {
      case INPUT -> (switch (size) {
        case TINY -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_TINY;
        case SMALL -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_BIG;
        case HUGE -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_LUDICROUS;
        case ULTIMATE -> MachineHatchTypeRegistration.ENERGY_INPUT_HATCH_ULTIMATE;
      }).get();
      case OUTPUT -> (switch(size) {
        case TINY -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_TINY;
        case SMALL -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_BIG;
        case HUGE -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_LUDICROUS;
        case ULTIMATE -> MachineHatchTypeRegistration.ENERGY_OUTPUT_HATCH_ULTIMATE;
      }).get();
      default -> null;
    };
  }

  public void resetTextures() {
    setMachineBaseTexture(defaultBaseTexture);
    setMachineOverlayTexture(defaultOverlayTexture);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(BooleanSyncable.create(() -> this.shouldAutoOutput, v -> this.shouldAutoOutput = v));
    container.accept(BooleanSyncable.create(() -> this.shouldAutoInput, v -> this.shouldAutoInput = v));
  }
}
