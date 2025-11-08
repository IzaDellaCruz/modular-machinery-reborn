package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.client.model.hatch.DefaultHatchBakedModel;
import es.degrassi.mmreborn.common.entity.base.BlockEntityRestrictedTick;
import es.degrassi.mmreborn.common.entity.base.IServerTickEntity;
import es.degrassi.mmreborn.common.entity.base.MachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.ChunkloadComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.Chunkloader;
import es.degrassi.mmreborn.common.util.ChunkloaderList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
@Getter
@Setter
public class ChunkloaderEntity extends BlockEntityRestrictedTick implements MachineComponentEntity<ChunkloadComponent>, TextureableMachineEntity,
    IServerTickEntity {
  private ResourceLocation baseTexture;
  private ResourceLocation overlayTexture;
  private static final ResourceLocation defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_chunkloader");
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");
  private final Chunkloader chunkloader;
  public ChunkloaderEntity(BlockPos pos, BlockState blockState) {
    super(EntityRegistration.CHUNKLOADER.get(), pos, blockState);
    chunkloader = new Chunkloader(this);
    this.overlayTexture = defaultOverlayTexture;
    this.baseTexture = defaultBaseTexture;
  }

  @Override
  public ChunkloadComponent provideComponent() {
    return new ChunkloadComponent(this);
  }

  @Override
  protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
    super.saveAdditional(nbt, pRegistries);
    nbt.putString("chunkloader", chunkloader.toString());
    if (baseTexture != null)
      nbt.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      nbt.putString("overlayTexture", overlayTexture.toString());
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    chunkloader.deserializeNBT(pRegistries, compound.getCompound("chunkloader"));

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;
  }

  @Override
  public void setLevel(Level level) {
    super.setLevel(level);
    ChunkloaderList.add(this);
  }

  @Override
  public void doRestrictedTick() {
    chunkloader.serverTick();
  }

  @Override
  public void onLoad() {
    super.onLoad();
    chunkloader.init();
  }

  @Getter
  private boolean unloaded = false;

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    this.unloaded = true;
  }

  @Override
  public void setRemoved() {
    chunkloader.onRemoved();
    super.setRemoved();
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
