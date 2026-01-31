package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.capability.EntityHandler;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.EntityComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;

@Getter
public abstract class EntityBaseEntity extends ColorableMachineComponentEntity implements MachineComponentEntity<EntityComponent>,
TextureableMachineEntity, ControllerAccessible {
  protected final EntityHandler handler;
  protected final IOType mode;

  @Getter
  @Setter
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

  private final RequirementEntity.Action[] validActions;

  public EntityBaseEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState,
                          RequirementEntity.Action... validActions) {
    super(entityType, pos, blockState);
    if (validActions.length < 1) throw new IllegalArgumentException("Actions must be at least 1");
    this.handler = new EntityHandler(this);
    this.mode = validActions[0].getMode();
    this.validActions = validActions;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_entity" + fromMode());
    this.overlayTexture = defaultOverlayTexture;
  }

  private String fromMode() {
    return switch (validActions[0]) {
      case ADD_HEALTH -> "healer";
      case CONSUME_HEALTH -> "damager";
      case KILL -> "killer";
      case SPAWN -> "spawner";
      case CHECK_AMOUNT, CHECK_HEALTH -> "detector";
    };
  }

  @Override
  public EntityComponent provideComponent() {
    return new EntityComponent(mode, handler, validActions);
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
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_entity" + fromMode());

    this.baseTexture = compound.contains("baseTexture") ? ResourceLocation.parse(compound.getString("baseTexture")) : defaultBaseTexture;
    this.overlayTexture = compound.contains("overlayTexture") ? ResourceLocation.parse(compound.getString("overlayTexture")) : defaultOverlayTexture;
    if (compound.contains("controllerPos")) {
      controllerPos = BlockPos.of(compound.getLong("controllerPos"));
    }
    ListTag validActions = compound.getList("validActions", StringTag.TAG_STRING);
    for (int i = 0; i < validActions.size(); i++) {
      var action = validActions.get(i);
      if (action instanceof StringTag st) {
        var validAction = RequirementEntity.Action.value(st.getAsString());
        this.validActions[i] = validAction;
      }
    }
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);
    if (baseTexture != null)
      compound.putString("baseTexture", baseTexture.toString());
    if (overlayTexture != null)
      compound.putString("overlayTexture", overlayTexture.toString());
    if (controllerPos != null)
      compound.putLong("controllerPos", controllerPos.asLong());
    ListTag validActions = new ListTag();
    for (var validAction : this.validActions) {
      validActions.add(new StringTag(validAction.name()));
    }
    compound.put("validActions", validActions);
  }

  @Override
  public MachineHatchType getHatchType() {
    return (switch (validActions[0]) {
      case CHECK_HEALTH, CHECK_AMOUNT -> MachineHatchTypeRegistration.ENTITY_DETECTOR;
      case KILL -> MachineHatchTypeRegistration.ENTITY_KILLER;
      case SPAWN -> MachineHatchTypeRegistration.ENTITY_SPAWNER;
      case ADD_HEALTH -> MachineHatchTypeRegistration.ENTITY_HEALER;
      case CONSUME_HEALTH -> MachineHatchTypeRegistration.ENTITY_DAMAGER;
    }).get();
  }
}
