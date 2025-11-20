package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.machine.SoundManagerEntity;
import es.degrassi.mmreborn.api.controller.ComponentMapper;
import es.degrassi.mmreborn.api.crafting.ComponentNotFoundException;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.IntegerSyncable;
import es.degrassi.mmreborn.api.network.syncable.NbtSyncable;
import es.degrassi.mmreborn.api.network.syncable.StringSyncable;
import es.degrassi.mmreborn.client.integration.athena.model.controller.ControllerBakedModel;
import es.degrassi.mmreborn.client.integration.athena.model.controller.ControllerData;
import es.degrassi.mmreborn.common.block.BlockMachineComponent;
import es.degrassi.mmreborn.common.crafting.helper.CraftingStatus;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.entity.base.BlockEntityRestrictedTick;
import es.degrassi.mmreborn.common.entity.base.BlockEntitySynchronized;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineEntity;
import es.degrassi.mmreborn.common.entity.base.IClientTickEntity;
import es.degrassi.mmreborn.common.entity.base.IServerTickEntity;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.util.sound.AmbientSound;
import es.degrassi.mmreborn.common.util.sound.Sounds;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessor;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.network.server.SMachineUpdatePacket;
import es.degrassi.mmreborn.common.network.server.SSyncPauseStatePacket;
import es.degrassi.mmreborn.common.network.server.SUpdateCraftingStatusPacket;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import es.degrassi.mmreborn.common.util.sound.SoundManager;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Getter
@Setter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineControllerEntity extends BlockEntityRestrictedTick implements ComponentMapper, ISyncableStuff,
    IServerTickEntity, IClientTickEntity, SoundManagerEntity {
  @Setter
  private CraftingStatus craftingStatus = CraftingStatus.MISSING_STRUCTURE;
  private boolean isPaused = false;
  private ResourceLocation id = DynamicMachine.DUMMY.getRegistryName();
  private MachineStatus status = MachineStatus.IDLE;
  private Component errorMessage = Component.empty();
  private final ComponentManager componentManager;
  private final MachineProcessor processor;
  private int lastFocus;
  private SoundManager soundManager;

  private final long tickOffset = Utils.RAND.nextIntBetweenInclusive(0, Integer.MAX_VALUE - 1);
  private long lastCheckTick;

  public MachineControllerEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.CONTROLLER.get(), pos, state);
    componentManager = new ComponentManager(this);
    processor = new MachineProcessor(this);
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    components.set(DataComponentRegistration.MACHINE_DATA, getId());
  }

  public void setStatus(MachineStatus status, Component message) {
    if (this.status != status) {
      this.componentManager.getFoundComponentsList().forEach(component -> component.onStatusChanged(this.status, status, message));
      this.status = status;
      this.errorMessage = message;
      setCraftingStatus(craftingByMachine(status));
      setRequestModelUpdate(true);
      setChanged();
      if (this.getLevel() instanceof ServerLevel sl) {
        BlockPos pos = this.getBlockPos();
        sl.updateNeighborsAt(pos, this.getBlockState().getBlock());
        PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos), new SUpdateCraftingStatusPacket(this.status, pos));
      }
    }
  }

  @Override
  public void setChanged() {
    if(this.level != null)
      this.level.blockEntityChanged(this.worldPosition);
  }

  private CraftingStatus craftingByMachine(MachineStatus status) {
    return switch (status) {
      case IDLE -> CraftingStatus.NO_RECIPE;
      case PAUSED -> craftingStatus;
      case ERRORED -> CraftingStatus.failure(errorMessage);
      case RUNNING -> CraftingStatus.working();
      case MISSING_STRUCTURE -> CraftingStatus.MISSING_STRUCTURE;
    };
  }

  public void setStatus(MachineStatus status) {
    this.setStatus(status, Component.empty());
  }

  @Override
  public ModelData getModelData() {
    return ModelData.builder()
        .with(ControllerBakedModel.DATA, new ControllerData(getFoundMachine(), null))
        .build();
  }

  public boolean isPaused() {
    return isPaused;
  }

  public void tryPause() {
    setPaused(RedstoneHelper.getReceivingRedstone(this) > 0);
  }

  public void setPaused(boolean paused) {
    if (paused) setStatus(MachineStatus.PAUSED);
    if (!getLevel().isClientSide && paused != isPaused) {
      PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) getLevel(), new ChunkPos(getBlockPos()),
          new SSyncPauseStatePacket(paused, getBlockPos()));
    }
    this.isPaused = paused;
  }

  @Override
  public void doClientTick() {
    if (soundManager == null)
      soundManager = new SoundManager(getBlockPos());
    AmbientSound sound = getFoundMachine().getAmbientSound(status);

    if (!soundManager.isCurrentlyPlaying(sound)) {
      if (sound == Sounds.DEFAULT.ambientSound()) {
        soundManager.setSound(null);
      } else {
        soundManager.setSound(sound);
      }
    }

    if (!soundManager.isPlaying())
      soundManager.play();
  }

  @Override
  public void doRestrictedTick() {
    IServerTickEntity.super.doRestrictedTick();
    tryPause();
    checkStructure(false);

    if (status.isMissingStructure()) {
      processor.reset();
      componentManager.reset();
      return;
    }

    if (isPaused()) return;

    componentManager.updateComponents(false);

    try {
      processor.tick();
    } catch (ComponentNotFoundException e) {
      ModularMachineryReborn.LOGGER.error(e.getMessage());
    }
  }

  @Override
  public void setRemoved() {
    if (this.level != null && this.level.isClientSide() && this.soundManager != null)
      this.soundManager.stop();
    super.setRemoved();
  }

  public void setMachine(ResourceLocation machine) {
    this.id = machine;
    distributeCasingColor(false, getBlockPos());
    if (getLevel() instanceof ServerLevel l)
      PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()), new SMachineUpdatePacket(id, getBlockPos()));
    setRequestModelUpdate(true);
    refreshClientData();
    setChanged();
  }

  public void checkStructure(boolean immediate) {
    if (this.getFoundMachine() == DynamicMachine.DUMMY)
      return;
    long gameTime = getLevel().getGameTime();
    if (!Utils.shouldRunPeriodicCheck(immediate, gameTime, lastCheckTick, tickOffset,
        MMRConfig.get().checkStructureTicks.get()))
      return;
    lastCheckTick = gameTime;
    if (!getFoundMachine().getPattern().match(getLevel(), getBlockPos(),
        getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
      distributeCasingColor(true);
      setStatus(MachineStatus.MISSING_STRUCTURE);
    } else {
      distributeCasingColor(false);
      componentManager.updateComponents(immediate);
      if (!status.isCrafting()) {
        setStatus(MachineStatus.IDLE);
      } else {
        setStatus(status);
      }
    }
    setRequestModelUpdate(true);
    setChanged();
  }

  public void distributeCasingColor(boolean default_, BlockPos... poss) {
    int color = default_ ? Config.machineColor : getFoundMachine().getMachineColor();
    for (BlockPos pos : poss) {
      if (getLevel().getBlockEntity(pos) instanceof MachineControllerEntity entity)
        tryColorize(pos, entity.getFoundMachine().getMachineColor(), default_);
      else
        tryColorize(this.getBlockPos().offset(pos), color, default_);
    }
    tryColorize(getBlockPos(), getFoundMachine().getMachineColor(), default_);
  }

  public void distributeCasingColor(boolean default_) {
    if (ModularMachineryReborn.MACHINES.get(id) != DynamicMachine.DUMMY) {
      distributeCasingColor(default_, getFoundMachine().getPattern().getBlocks(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)).keySet().toArray(BlockPos[]::new));
    } else {
      if (!getFoundMachine().getPattern().match(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
        BlockPos[] blockPos = getFoundMachine().getPattern().getBlocks(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)).keySet().toArray(BlockPos[]::new);
        distributeCasingColor(true, blockPos);
        for (BlockPos pos : blockPos) {
          if (getLevel().getBlockEntity(getBlockPos().offset(pos)) instanceof BlockEntitySynchronized entity) {
            entity.setInStructure(false);
            entity.setChanged();
          }
        }
      }
    }
  }

  private void tryColorize(BlockPos pos, int color, boolean _default) {
    BlockEntity te = this.getLevel().getBlockEntity(pos);
    AtomicBoolean shouldColor = new AtomicBoolean(true);
    if (te instanceof TextureableMachineEntity entity) {
      entity.resetTextures();
      if (!_default) {
        Optional.ofNullable(getFoundMachine().getFormedTextures().get(entity.getHatchType())).ifPresent(p -> {
          shouldColor.set(p.getFirst());
          p.mapSecond(pair -> {
            pair.getFirst().ifPresent(entity::setMachineBaseTexture);
            pair.getSecond().ifPresent(entity::setMachineOverlayTexture);
            return null;
          });
        });
      }
      te.setChanged();
    }

    if (te instanceof ColorableMachineEntity entity) {
      entity.setMachineColor(shouldColor.get() ? color : 0xffffffff);
      te.setChanged();
    }
  }

  public DynamicMachine getFoundMachine() {
    return ModularMachineryReborn.MACHINES.getOrDefault(id, DynamicMachine.DUMMY);
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.loadAdditional(compound, pRegistries);
    this.craftingStatus = CraftingStatus.deserialize(compound.getCompound("status"), pRegistries);
    this.id = ResourceLocation.parse(compound.getString("machine"));
    processor.deserialize(compound.getCompound("craftingManager"));
    this.isPaused = compound.getBoolean("isPaused");
    if (getLevel() != null && !getLevel().isClientSide)
      checkStructure(true);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
    super.saveAdditional(compound, pRegistries);
    compound.put("status", this.craftingStatus.serializeNBT(pRegistries));
    compound.putString("machine", id.toString());
    compound.put("craftingManager", processor.serialize());
    compound.put("componentManager", componentManager.serializeNBT(pRegistries));
    compound.putBoolean("isPaused", isPaused);
  }

  public void refreshClientData() {
    requestModelDataUpdate();
  }

  public List<MachineComponent<?>> getFoundComponents() {
    return componentManager.getFoundComponentsList();
  }

  @Override
  public Map<BlockPos, MachineComponent<?>> getFoundComponentsMap() {
    return componentManager.getFoundComponentsMap();
  }

  public List<ModifierReplacement> getFoundModifiers() {
    return componentManager.getFoundModifiersList();
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    if (this.getLevel() == null)
      return;
    processor.getStuffToSync(container);
    componentManager.getStuffToSync(container);
    RegistryAccess registries = this.getLevel().registryAccess();
    container.accept(StringSyncable.create(() -> id.toString(), s -> id = ResourceLocation.parse(s)));
    container.accept(IntegerSyncable.create(() -> lastFocus, i -> lastFocus = i));
    container.accept(NbtSyncable.create(() -> craftingStatus.serializeNBT(getLevel().registryAccess()), s -> craftingStatus = CraftingStatus.deserialize(s, getLevel().registryAccess())));
    container.accept(StringSyncable.create(() -> this.status.toString(), status -> this.status = MachineStatus.value(status)));
    container.accept(StringSyncable.create(() -> Component.Serializer.toJson(this.errorMessage, registries), errorMessage -> this.errorMessage = Component.Serializer.fromJson(errorMessage, registries)));
  }

  public SoundType getInteractionSound() {
    return getFoundMachine().getInteractionSound(status);
  }
}
