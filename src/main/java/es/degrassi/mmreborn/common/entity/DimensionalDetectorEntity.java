package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.model.hatch.DefaultHatchBakedModel;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.MachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.DimensionComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;

@MethodsReturnNonnullByDefault
@Getter
@Setter
public class DimensionalDetectorEntity extends ColorableMachineComponentEntity implements MachineComponentEntity<DimensionComponent>, TextureableMachineEntity {
  private ResourceLocation baseTexture;
  private ResourceLocation overlayTexture;
  private static final ResourceLocation defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_dimensional_detector");
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");
  public DimensionalDetectorEntity(BlockPos pos, BlockState blockState) {
    super(EntityRegistration.DIMENSIONAL_DETECTOR.get(), pos, blockState);
    this.overlayTexture = defaultOverlayTexture;
    this.baseTexture = defaultBaseTexture;
  }

  @Override
  public DimensionComponent provideComponent() {
    return new DimensionComponent(this);
  }

  @Override
  protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
    super.saveAdditional(nbt, pRegistries);
    if (baseTexture != null)
      nbt.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      nbt.putString("overlayTexture", overlayTexture.toString());
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;
  }

  @Override
  public ModelData getModelData() {
    ModelData.Builder builder = getModelDataBuilder("all");
    builder.with(DefaultHatchBakedModel.BASE_TEXTURE, baseTexture)
        .with(DefaultHatchBakedModel.BASE_TEXTURE_NAME, "bg_all");
    builder.with(DefaultHatchBakedModel.OVERLAY_TEXTURE, overlayTexture)
        .with(DefaultHatchBakedModel.OVERLAY_TEXTURE_NAME, "ov_all");
    return builder.build();
  }

  @Override
  public ResourceLocation getMachineBaseTexture() {
    return baseTexture;
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
  public ResourceLocation getMachineOverlayTexture() {
    return overlayTexture;
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
    return MachineHatchTypeRegistration.BIOME_READER.get();
  }

  public void resetTextures() {
    setMachineBaseTexture(defaultBaseTexture);
    setMachineOverlayTexture(defaultOverlayTexture);
  }
}
