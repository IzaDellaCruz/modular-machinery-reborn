package es.degrassi.mmreborn.common.entity.base;

import com.google.common.collect.Maps;
import es.degrassi.experiencelib.api.capability.ExperienceLibCapabilities;
import es.degrassi.experiencelib.api.capability.IExperienceHandler;
import es.degrassi.experiencelib.impl.capability.BasicExperienceHandler;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.BooleanSyncable;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchTextureData;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.ExperienceInputHatchEntity;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.component.ExperienceComponent;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateExperienceComponentPacket;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class ExperienceHatchEntity extends ColorableMachineComponentEntity implements MachineComponentEntity<ExperienceComponent>,
    ControllerAccessible, TextureableMachineEntity, CapabilityInventoryEntity<IExperienceHandler>, ITickEntity, IServerTickEntity, ISyncableStuff,
    IAutoEntity<IExperienceHandler> {
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
  @Getter
  private final IOInventory capabilityInventory;

  private final long tickOffset = Utils.RAND.nextIntBetweenInclusive(0, Integer.MAX_VALUE - 1);
  private long lastCheckTick;

  @Getter
  private final Map<Direction, BlockCapabilityCache<IExperienceHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

  protected ExperienceHatchEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ExperienceHatchSize size,
                          IOType ioType) {
    super(type, pos, state);
    this.size = size;
    this.ioType = ioType;
    this.defaultOverlayTexture = ModularMachineryReborn.rl("block/overlay_experience" + ioType.getSerializedName() + "hatch_" + size.getSerializedName());
    this.overlayTexture = defaultOverlayTexture;
    this.experienceTank = buildTank();
    this.capabilityInventory = this.createCapabilityInventory();
    this.shouldAutoOutput = ioType.isOutput();
    this.shouldAutoInput = ioType.isInput();
  }

  @Override
  public void doRestrictedTick() {
    IServerTickEntity.super.doRestrictedTick();
    tickInventory();
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
  public void tickInventory() {
    if (!shouldTickInventory()) return;
    capabilityInventory.getInventory().forEach(slot -> {
      Optional.ofNullable(slot.getItemStack().getCapability(getCapability())).ifPresent(cap -> {
        if (ioType == IOType.NONE) return;
        if (ioType.isInput()) {
          if (!cap.canExtract(0)) return;
          if (this.getTank().getExperience() >= this.getTank().getExperienceCapacity()) return;
          if (slot.getItemStack().is(Items.EXPERIENCE_BOTTLE)) {
            int experienceBottles = slot.getItemStack().getCount();
            for (int i = 0; i < experienceBottles; i++) {
              long simulatedCap = cap.extractExperience(0, Long.MAX_VALUE, true);
              long simulatedInsert = getTank().receiveExperienceRecipe(0, simulatedCap, true);
              if (simulatedInsert < 7) return;
              ItemStack stack = slot.extractItemBypassLimit(1, true);
              if (stack.isEmpty()) return;
              slot.extractItemBypassLimit(1, false);
              cap.extractExperienceRecipe(0, simulatedInsert, false);
              getTank().receiveExperience(0, simulatedInsert, false);
            }
          } else {
            if (slot.getItemStack().is(Items.GLASS_BOTTLE)) return;
            long simulatedCap = cap.extractExperience(0, Long.MAX_VALUE, true);
            long simulatedInsert = getTank().receiveExperience(0, simulatedCap, true);
            cap.extractExperienceRecipe(0, simulatedInsert, false);
            getTank().receiveExperienceRecipe(0, simulatedInsert, false);
          }
        } else if (ioType.isOutput()) {
          if (!cap.canReceive(0)) return;
          if (this.getTank().getExperience() == 0) return;
          if (slot.getItemStack().is(Items.GLASS_BOTTLE)) {
            int experienceBottles = slot.getItemStack().getCount();
            if (experienceBottles > 1) return;
            long simulatedCap = cap.receiveExperienceRecipe(0, Long.MAX_VALUE, true);
            long simulatedInsert = getTank().extractExperienceRecipe(0, simulatedCap, true);
            if (simulatedInsert < 7) return;
            ItemStack extracted = slot.extractItemBypassLimit(1, true);
            if (extracted.isEmpty()) return;
            ItemStack stack = slot.insertItemBypassLimit(new ItemStack(Items.EXPERIENCE_BOTTLE, 1), true);
            if (stack.isEmpty()) return;
            slot.extractItemBypassLimit(1, false);
            slot.insertItemBypassLimit(new ItemStack(Items.EXPERIENCE_BOTTLE, 1), false);
            cap.receiveExperienceRecipe(0, simulatedInsert, false);
            getTank().extractExperienceRecipe(0, simulatedInsert, false);
          } else {
            if (slot.getItemStack().is(Items.EXPERIENCE_BOTTLE)) return;
            long simulatedCap = cap.receiveExperienceRecipe(0, Long.MAX_VALUE, true);
            long simulatedInsert = getTank().extractExperienceRecipe(0, simulatedCap, true);
            cap.receiveExperienceRecipe(0, simulatedInsert, false);
            getTank().extractExperienceRecipe(0, simulatedInsert, false);
          }
        }
      });
    });
  }

  @Override
  public ItemCapability<IExperienceHandler, Void> getCapability() {
    return ExperienceLibCapabilities.EXPERIENCE.item();
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
          getControllerPosSet().forEach(p -> {
            if (getLevel() == null) return;
            if (getLevel().isClientSide()) return;
            if (getLevel().getBlockEntity(p) instanceof MachineControllerEntity controller) {
              controller.getProcessor().setMachineInventoryChanged();
            }
          });
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
    this.capabilityInventory.deserialize(compound.getCompound("inventory"), pRegistries);

    this.shouldAutoOutput = ioType.isOutput() && shouldAutoOutput;
    this.shouldAutoInput = ioType.isInput() && shouldAutoInput;
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
    compound.put("inventory", this.capabilityInventory.writeNBT(pRegistries));
  }

  @Override
  public void setControllerPos(BlockPos pos) {
    this.controllerPos = pos;
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

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(BooleanSyncable.create(() -> this.shouldAutoOutput, v -> this.shouldAutoOutput = v));
    container.accept(BooleanSyncable.create(() -> this.shouldAutoInput, v -> this.shouldAutoInput = v));
  }

  protected void attemptXPTransfer(IExperienceHandler from, IExperienceHandler to, long maxTransfer) {
    for (int i = 0; i < from.getTanks(); i++) {
      if (!from.canExtract(i)) continue;
      long extracted = from.extractExperience(i, maxTransfer, true);
      if (extracted <= 0) continue;
      for (int j = 0; j < to.getTanks(); j++) {
        if (!to.canReceive(i)) continue;
        long inserted = to.receiveExperience(j, extracted, false);
        if (inserted < 1) continue;
        from.extractExperience(i, inserted, false);
      }
    }
  }
}
