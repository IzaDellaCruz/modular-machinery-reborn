package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.client.model.hatch.HatchBakedModel;
import es.degrassi.mmreborn.common.block.prop.FluidHatchSize;
import es.degrassi.mmreborn.common.entity.FluidInputHatchEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.HybridTank;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.Utils;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

@Getter
@Setter
public abstract class FluidTankEntity extends ColorableMachineComponentEntity implements MachineComponentEntity<FluidComponent>, ControllerAccessible,
    TextureableMachineEntity, CapabilityInventoryEntity<IFluidHandlerItem>, ITickEntity, IServerTickEntity {
  private HybridTank tank;
  private IOType ioType;
  private FluidHatchSize hatchSize;
  private BlockPos controllerPos;

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

  @Getter
  private final IOInventory capabilityInventory;

  private final long tickOffset = Utils.RAND.nextIntBetweenInclusive(0, Integer.MAX_VALUE - 1);
  private long lastCheckTick;

  protected FluidTankEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, FluidHatchSize size,
                           IOType ioType) {
    super(type, pos, state);
    this.tank = size.buildTank(this, ioType == IOType.INPUT, ioType == IOType.OUTPUT);
    this.hatchSize = size;
    this.ioType = ioType;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_fluid" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.capabilityInventory = createCapabilityInventory();

    this.tank.setListener(() -> {
      if (getController() != null)
        getController().getProcessor().setMachineInventoryChanged();
    });
  }

  @Override
  public ItemCapability<IFluidHandlerItem, Void> getCapability() {
    return Capabilities.FluidHandler.ITEM;
  }

  @Override
  public void doRestrictedTick() {
    IServerTickEntity.super.doRestrictedTick();
    tickInventory();
  }

  @Override
  public void tickInventory() {
    if (!shouldTickInventory()) return;
    capabilityInventory.getInventory().forEach(slot -> {
      Optional.ofNullable(slot.getItemStack().getCapability(getCapability())).ifPresent(cap -> {
        if (ioType == IOType.NONE) return;
        if (ioType.isInput()) {
          if (this.getTank().getFluidAmount() >= this.getTank().getCapacity()) return;
          FluidStack simulatedCap = cap.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
          int simulatedInsert = getTank().fill(simulatedCap.copy(), IFluidHandler.FluidAction.SIMULATE);
          cap.drain(simulatedCap.copyWithAmount(simulatedInsert), IFluidHandler.FluidAction.EXECUTE);
          getTank().fill(simulatedCap.copyWithAmount(simulatedInsert), IFluidHandler.FluidAction.EXECUTE);
        } else if (ioType.isOutput()) {
          if (this.getTank().getFluidAmount() == 0) return;
          FluidStack simulatedExtract = getTank().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
          int simulatedCap = cap.fill(simulatedExtract.copy(), IFluidHandler.FluidAction.SIMULATE);
          cap.fill(simulatedExtract.copyWithAmount(simulatedCap), IFluidHandler.FluidAction.EXECUTE);
          getTank().drain(simulatedExtract.copyWithAmount(simulatedCap), IFluidHandler.FluidAction.EXECUTE);
        }
      });
    });
  }

  @Override
  public IOType getMode() {
    return ioType;
  }

  public boolean shouldTickInventory() {
    long gameTime = getLevel().getGameTime();
    if (!Utils.shouldRunPeriodicCheck(false, gameTime, lastCheckTick, tickOffset, 2))
      return false;
    lastCheckTick = gameTime;
    return true;
  }

  @Override
  public FluidComponent provideComponent() {
    return new FluidComponent(this.getTank(), ioType);
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
    super.loadAdditional(compound, provider);
    this.ioType = IOType.getByString(compound.getString("ioType"));
    this.hatchSize = FluidHatchSize.value(compound.getString("size"));
    HybridTank newTank = hatchSize.buildTank(this, ioType == IOType.INPUT, ioType == IOType.OUTPUT);
    CompoundTag tankTag = compound.getCompound("tank");
    newTank.readFromNBT(provider, tankTag);
    this.tank = newTank;
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_fluid" + ioType.getSerializedName() + "hatch_" + hatchSize.getSerializedName());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;

    this.tank.setListener(() -> {
      if (getController() != null)
        getController().getProcessor().setMachineInventoryChanged();
    });
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
    super.saveAdditional(compound, provider);
    if (ioType == null) {
      ioType = this instanceof FluidInputHatchEntity ? IOType.INPUT : IOType.OUTPUT;
    }
    compound.putString("ioType", ioType.getSerializedName());
    compound.putString("size", this.hatchSize.getSerializedName());
    CompoundTag tankTag = new CompoundTag();
    this.tank.writeToNBT(provider, tankTag);
    compound.put("tank", tankTag);
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
    return switch(ioType) {
      case INPUT -> (switch (hatchSize) {
        case TINY -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_TINY;
        case SMALL -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_BIG;
        case HUGE -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_LUDICROUS;
        case VACUUM -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_VACUUM;
      }).get();
      case OUTPUT -> (switch(hatchSize) {
        case TINY -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_TINY;
        case SMALL -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_BIG;
        case HUGE -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.FLUID_OUTPUT_HATCH_LUDICROUS;
        case VACUUM -> MachineHatchTypeRegistration.FLUID_INPUT_HATCH_VACUUM;
      }).get();
      default -> null;
    };
  }
}
