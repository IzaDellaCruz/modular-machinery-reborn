package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.experiencelib.impl.capability.BasicExperienceHandler;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.client.model.hatch.HatchBakedModel;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.ExperienceInputHatchEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.ExperienceComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateExperienceComponentPacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Locale;

public abstract class ExperienceHatchEntity extends ColorableMachineComponentEntity implements MachineComponentEntity<ExperienceComponent>, ControllerAccessible, TextureableMachineEntity {
  protected ExperienceHatchSize size;
  protected IOType ioType;
  @Getter
  private BlockPos controllerPos;

  private final BasicExperienceHandler experienceTank;

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

  protected ExperienceHatchEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ExperienceHatchSize size,
                          IOType ioType) {
    super(type, pos, state);
    this.size = size;
    this.ioType = ioType;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_experience" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.experienceTank = buildTank();
  }

  public BasicExperienceHandler getTank() {
    return experienceTank;
  }

  @Nullable
  @Override
  public ExperienceComponent provideComponent() {
    return new ExperienceComponent(this.getTank(), ioType);
  }

  private BasicExperienceHandler buildTank() {
    return new BasicExperienceHandler(
        1,
        size == null ? 0 : size.getCapacity(),
        () -> {
          if (getLevel() != null && !getLevel().isClientSide)
            PacketDistributor.sendToPlayersTrackingChunk(
                (ServerLevel) getLevel(),
                new ChunkPos(getBlockPos()),
                new SUpdateExperienceComponentPacket(getTank().getExperience(), getBlockPos())
            );
          if (getController() != null)
            getController().getProcessor().setMachineInventoryChanged();
        }
    ) {
      @Override
      public boolean canExtract(int tank) {
        return ioType == null || !ioType.isInput();
      }

      @Override
      public boolean canReceive(int tank) {
        return ioType == null || ioType.isInput();
      }

      @Override
      public boolean canAcceptExperience(int tank, long l) {
        return canReceive(tank) && receiveExperience(tank, l, true) > 0;
      }

      @Override
      public boolean canProvideExperience(int tank, long l) {
        return canExtract(tank) && extractExperience(tank, l, true) > 0;
      }

      @Override
      public long getMaxExtract(int tank) {
        return canExtract(tank) ? getExperienceCapacity() : 0;
      }

      @Override
      public long getMaxReceive(int tank) {
        return canReceive(tank) ? getExperienceCapacity() : 0;
      }
    };
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.size = ExperienceHatchSize.value(compound.getString("hatchSize").toUpperCase(Locale.ROOT));
    this.ioType = IOType.getByString(compound.getString("ioType"));

    if (compound.contains("experience", Tag.TAG_COMPOUND))
      this.experienceTank.deserializeNBT(pRegistries, compound.getCompound("experience"));
    for (int i = 0; i < experienceTank.getTanks(); i++) {
      experienceTank.setCapacity(i, size.getCapacity());
    }
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_experience" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);
    compound.putString("hatchSize", this.size.getSerializedName());
    if (ioType == null) {
      ioType = this instanceof ExperienceInputHatchEntity ? IOType.INPUT : IOType.OUTPUT;
    }
    compound.putString("ioType", ioType.getSerializedName());

    compound.put("experience", experienceTank.serializeNBT(pRegistries));
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
      case INPUT -> (switch (size) {
        case TINY -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_TINY;
        case SMALL -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_BIG;
        case HUGE -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_LUDICROUS;
        case VACUUM -> MachineHatchTypeRegistration.EXPERIENCE_INPUT_HATCH_VACUUM;
      }).get();
      case OUTPUT -> (switch(size) {
        case TINY -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_TINY;
        case SMALL -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_SMALL;
        case NORMAL -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_NORMAL;
        case REINFORCED -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_REINFORCED;
        case BIG -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_BIG;
        case HUGE -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_HUGE;
        case LUDICROUS -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_LUDICROUS;
        case VACUUM -> MachineHatchTypeRegistration.EXPERIENCE_OUTPUT_HATCH_VACUUM;
      }).get();
      default -> null;
    };
  }
}
