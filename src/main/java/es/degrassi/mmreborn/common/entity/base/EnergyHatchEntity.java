package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.client.model.hatch.HatchBakedModel;
import es.degrassi.mmreborn.common.block.prop.EnergyHatchSize;
import es.degrassi.mmreborn.common.entity.EnergyInputHatchEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateEnergyComponentPacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.IEnergyHandler;
import es.degrassi.mmreborn.common.util.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Locale;

public abstract class EnergyHatchEntity extends ColorableMachineComponentEntity implements IEnergyHandler,
    MachineComponentEntity<EnergyComponent>, ControllerAccessible, TextureableMachineEntity {

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
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");

  protected EnergyHatchEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, EnergyHatchSize size,
                          IOType ioType) {
    super(type, pos, state);
    this.size = size;
    this.ioType = ioType;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_energy" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
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
      if (getController() != null)
        getController().getProcessor().setMachineInventoryChanged();
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
      if (getController() != null)
        getController().getProcessor().setMachineInventoryChanged();
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
    if (getController() != null)
      getController().getProcessor().setMachineInventoryChanged();
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_energy" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;
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
    if (getController() != null)
      getController().getProcessor().setMachineInventoryChanged();
    markForUpdate();
  }

  @Override
  public long getMaxEnergy() {
    return this.size.maxEnergy;
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
}
