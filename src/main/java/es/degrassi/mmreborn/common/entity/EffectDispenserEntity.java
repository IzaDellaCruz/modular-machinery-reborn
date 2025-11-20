package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.capability.EffectHandler;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.BooleanSyncable;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.common.block.prop.EffectDispenserSize;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.IServerTickEntity;
import es.degrassi.mmreborn.common.entity.base.ITickEntity;
import es.degrassi.mmreborn.common.entity.base.MachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.EffectComponent;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@Setter
public class EffectDispenserEntity extends ColorableMachineComponentEntity implements MachineComponentEntity<EffectComponent>,
    ControllerAccessible, TextureableMachineEntity, ISyncableStuff, ITickEntity, IServerTickEntity {
  @Getter
  private BlockPos controllerPos;
  @Getter
  private ResourceLocation baseTexture;
  @Getter
  private ResourceLocation overlayTexture;
  @Getter
  private static final ResourceLocation defaultOverlayTexture = ModularMachineryReborn.rl("block/effect_dispenser");
  @Getter
  private static final ResourceLocation defaultBaseTexture = ModularMachineryReborn.rl("block/casing_plain");

  @Getter
  private EffectDispenserSize size;
  @Getter
  private final EffectHandler handler;

  public EffectDispenserEntity(BlockPos pos, BlockState blockState, EffectDispenserSize size) {
    super(EntityRegistration.EFFECT_DISPENSER.get(), pos, blockState);
    this.size = size;
    this.handler = new EffectHandler(size, this);
  }

  public EffectDispenserEntity(BlockPos pos, BlockState state) {
    this(pos, state, EffectDispenserSize.SMALL);
  }

  @Override
  public void doRestrictedTick() {
    IServerTickEntity.super.doRestrictedTick();
    if (getController() == null || getController().getStatus() != MachineStatus.RUNNING) {
      if (getEffect().isPresent())
        this.getHandler().resetEffect();
    }
  }

  @Override
  public @Nullable EffectComponent provideComponent() {
    return new EffectComponent(handler);
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
    return (switch (size) {
      case SMALL -> MachineHatchTypeRegistration.EFFECT_DISPENSER_SMALL;
      case MEDIUM -> MachineHatchTypeRegistration.EFFECT_DISPENSER_MEDIUM;
      case BIG -> MachineHatchTypeRegistration.EFFECT_DISPENSER_BIG;
    }).get();
  }

  @Override
  public void resetTextures() {
    setMachineBaseTexture(defaultBaseTexture);
    setMachineOverlayTexture(defaultOverlayTexture);
  }

  @Override
  protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
    super.loadAdditional(nbt, pRegistries);
    this.size = EffectDispenserSize.value(nbt.getString("busSize"));
    if (nbt.contains("controllerPos")) {
      controllerPos = BlockPos.of(nbt.getLong("controllerPos"));
    }

    this.baseTexture = nbt.contains("baseTexture") ? ResourceLocation.parse(nbt.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = nbt.contains("overlayTexture") ? ResourceLocation.parse(nbt.getString("overlayTexture")) : defaultOverlayTexture;

    this.handler.deserializeNBT(pRegistries, nbt.getCompound("effectHandler"));
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

    compound.put("effectHandler", handler.serializeNBT(pRegistries));
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
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(BooleanSyncable.create(handler::isApplyingEffect, handler::setApplyingEffect));
  }

  public boolean isApplyingEffect() {
    return handler.isApplyingEffect();
  }

  public Optional<MobEffectInstance> getEffect() {
    return Optional.ofNullable(handler.getEffect());
  }
}
