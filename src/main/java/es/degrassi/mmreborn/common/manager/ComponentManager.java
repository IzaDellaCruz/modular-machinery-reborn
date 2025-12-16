package es.degrassi.mmreborn.common.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.crafting.ComponentNotFoundException;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.client.integration.athena.model.controller.ControllerBakedModel;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchBakedModel;
import es.degrassi.mmreborn.common.block.BlockMachineComponent;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.crafting.modifier.RecipeModifier;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.MachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.machine.component.FunctionComponent;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.machine.component.ParallelComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ComponentManager implements INBTSerializable<CompoundTag>, ISyncableStuff {
  @Getter
  private final MachineControllerEntity controller;

  public static final LoadingCache<MachineControllerEntity, List<BlockPos>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
    @Override
    public @NotNull List<BlockPos> load(MachineControllerEntity key) {
      BlockPos pos = key.getBlockPos();
      return key
          .getFoundMachine()
          .getPattern()
          .getBlocksFiltered(key.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
          .entrySet()
          .stream()
          .filter(e -> !e.getValue().equals(BlockIngredient.MACHINE))
          .map(Map.Entry::getKey)
          .map(pos::offset)
          .toList();
    }
  });

  private final LoadingCache<BlockPos, Optional<MachineComponent<?>>> fC;
  private final LoadingCache<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> fCV;
  private final LoadingCache<BlockPos, List<ModifierReplacement>> fM;
  private final LoadingCache<RequirementType<?, ?, ?>, List<RecipeModifier<?, ?, ?>>> fMV;

  public ComponentManager(MachineControllerEntity entity) {
    this.controller = entity;
    this.fC = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
          @Override
          public @NotNull Optional<MachineComponent<?>> load(BlockPos key) {
            if (controller.getLevel() == null) return Optional.empty();
            if (key.equals(controller.getBlockPos())) return Optional.of(new FunctionComponent(key));
            if (controller.getLevel().getBlockEntity(key) instanceof MachineComponentEntity<?> e) {
              return Optional.ofNullable(e.provideComponent());
            }
            return Optional.empty();
          }
        });
    this.fCV = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
          @Override
          public @NotNull Map<IOType, List<MachineComponent<?>>> load(ComponentType<?> key) {
            Map<IOType, List<MachineComponent<?>>> foundComponentsValues = Maps.newHashMap();
            for (MachineComponent<?> comp :
                fC.asMap()
                    .values()
                    .stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(c -> c.getComponentType().equals(key))
                    .toList()) {
                foundComponentsValues.computeIfAbsent(comp.getIOType(), io -> Lists.newArrayList()).add(comp);
            }
            return foundComponentsValues;
          }
        });
    this.fM = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
          @Override
          public @NotNull List<ModifierReplacement> load(BlockPos key) {
            if (controller.getLevel() == null) return Collections.emptyList();
            return controller.getFoundMachine()
                .getPattern()
                .getPattern()
                .getModifiers(controller.getFacing())
                .entrySet()
                .stream()
                .filter((entry) -> {
                  BlockPos realPos = controller.getBlockPos().offset(entry.getKey());
                  BlockInWorld biw = new BlockInWorld(controller.getLevel(), realPos, false);
                  return (realPos.equals(key)
                      && entry.getValue()
                      .stream()
                      .anyMatch(modifier -> modifier.getIngredient().getAll().stream().anyMatch(state -> state.test(biw))));
                })
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .toList();
          }
        });
    this.fMV = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
          @Override
          public @NotNull List<RecipeModifier<?, ?, ?>> load(RequirementType<?, ?, ?> key) {
            return fM.asMap()
                .values()
                .stream()
                .flatMap(List::stream)
                .map(ModifierReplacement::getModifiers)
                .flatMap(List::stream)
                .filter(r -> r.getRequirementType().equals(key))
                .toList();
          }
        });
  }

  public final void reset() {
    fC.invalidateAll();
    fM.invalidateAll();
    fCV.invalidateAll();
    fMV.invalidateAll();
  }

  public final void resetWithColor() {
    try {
      for (BlockPos current : cache.get(controller).stream().toList()) {
        if (Objects.requireNonNull(controller.getLevel()).getBlockEntity(current) instanceof ColorableMachineComponentEntity entity) {
          entity.getControllerPosSet().remove(controller.getBlockPos());
          entity.setMachineColor(Config.machineColor);
          if (entity instanceof TextureableMachineEntity e) e.resetTextures();
        }
      }
    } catch (ExecutionException | NullPointerException ignored) {}
    fC.invalidateAll();
    fM.invalidateAll();
    fCV.invalidateAll();
    fMV.invalidateAll();
  }

  public final void updateComponents() {
    if (controller.getFoundMachine() == DynamicMachine.DUMMY) return;
    Level level = controller.getLevel();
    if (level == null) return;
    resetWithColor();
    cache.refresh(controller);
    if (controller.getModelData().get(ControllerBakedModel.DATA).hasCustomModel()) {
      controller.getLevel().setBlockAndUpdate(controller.getBlockPos(), controller.getBlockState().setValue(BlockMachineComponent.CONNECT_TEXTURES, false));
    } else if(!controller.getBlockState().getValue(BlockMachineComponent.CONNECT_TEXTURES)) {
      controller.getLevel().setBlockAndUpdate(controller.getBlockPos(), controller.getBlockState().setValue(BlockMachineComponent.CONNECT_TEXTURES, true));
    }
    try {
      Set<ComponentType<?>> toRefreshComponent = Sets.newHashSet();
      Set<RequirementType<?, ?, ?>> toRefreshRequirement = Sets.newHashSet();
      cache.get(controller).forEach(pos -> {
        var oldState = controller.getLevel().getBlockState(pos);
        var entity = controller.getLevel().getBlockEntity(pos);
        if (entity instanceof ColorableMachineComponentEntity ce) {
          ce.getControllerPosSet().add(controller.getBlockPos());
        }
        if (!(entity instanceof TextureableMachineEntity)) return;
        var data = entity.getModelData();
        if (!data.has(HatchBakedModel.TEXTURE_DATA)) return;
        var state = oldState.setValue(BlockMachineComponent.CONNECT_TEXTURES,
            data.get(HatchBakedModel.TEXTURE_DATA).hasDefaultTextures());
        try {
          if (entity instanceof MachineComponentEntity<?>) {
            fC.refresh(pos);
            fC.get(pos)
                .map(MachineComponent::getComponentType)
                .ifPresent(toRefreshComponent::add);
            fM.refresh(pos);
            fM.get(pos)
                .stream()
                .map(ModifierReplacement::getModifiers)
                .flatMap(List::stream)
                .map(RecipeModifier::getRequirementType)
                .forEach(toRefreshRequirement::add);
          }
          toRefreshComponent.forEach(fCV::refresh);
          toRefreshRequirement.forEach(fMV::refresh);
        } catch (ExecutionException ignored) {}
        controller.getLevel().setBlockAndUpdate(pos, state);
      });
      controller.setChanged();
    } catch(ExecutionException ignored) {}
  }

  @SuppressWarnings("unchecked")
  public final List<MachineComponent<?>> getFoundComponentsList() {
    return (List<MachineComponent<?>>) (Object) fC.asMap()
        .values()
        .stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  public List<ModifierReplacement> getFoundModifiersList() {
    return fM.asMap()
        .values()
        .stream()
        .flatMap(List::stream)
        .toList();
  }

  public final Map<BlockPos, Optional<MachineComponent<?>>> getFoundComponentsMap() {
    return fC.asMap();
  }

  public final Map<BlockPos, List<ModifierReplacement>> getFoundModifiersMap() {
    return fM.asMap();
  }

  @SuppressWarnings("unchecked")
  public <R extends IRequirement<C, T>, C extends MachineComponent<T>, T> List<RecipeModifier<R, C, T>> getModifiers(RequirementType<R, C, T> type) {
    try {
      return (List<RecipeModifier<R, C, T>>) (Object) fMV.get(type);
    } catch (ExecutionException e) {
      return List.of();
    }
  }

  public <C extends MachineComponent<T>, T> Optional<C> getComponent(IRequirement<C, T> requirement, ICraftingContext context) {
    try {
      if (requirement.getType().equals(RequirementTypeRegistration.DURABILITY.get())) {
        return getComponent(requirement.getComponentType(), IOType.INPUT);
      }
      return getComponent(requirement.getComponentType(), requirement.getMode());
    } catch(Exception e) {
      return Optional.empty();
    }
  }

  public Optional<ParallelComponent> getParallel() {
    try {
      return getComponent(ComponentRegistration.COMPONENT_PARALLEL.get(), IOType.INPUT);
    } catch(Exception e) {
      return Optional.empty();
    }
  }

  public Optional<ItemComponent> getItemComponent(IOType mode) {
    try {
      return getComponent(ComponentRegistration.COMPONENT_ITEM.get(), mode);
    } catch(Exception e) {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<T>, T> Optional<C> getComponent(ComponentType<T> type, IOType mode) {
    try {
      return fCV.get(type).get(mode)
          .stream()
          .map(c -> (C) c)
          .filter(Objects::nonNull)
          .sorted()
          .reduce((c1, c2) -> {
            if (c1.canMerge(c2)) return c1.merge(c2);
            return c1;
          });
    } catch (ExecutionException ignored) {
      throw new ComponentNotFoundException(controller.getFoundMachine(), type);
    }
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();
    CompoundTag componentsByType = new CompoundTag();
    fCV.asMap().forEach((type, map) -> {
      CompoundTag listByMode = new CompoundTag();
      map.forEach((mode, list) -> listByMode.put(
          mode.getSerializedName(),
          getComponent(type, mode).map(component -> component.asTag(provider)).orElse(new CompoundTag())
      ));
      componentsByType.put(type.getId().toString(), listByMode);
    });
    nbt.put("components", componentsByType);
    ListTag modifiers = new ListTag();
    fM.asMap().forEach((pos, list) -> {
      ListTag mods = list.stream().map(ModifierReplacement::asTag).collect(ListTag::new, ListTag::add, ListTag::add);
      CompoundTag mod = new CompoundTag();
      CompoundTag position = new CompoundTag();
      position.putInt("x", pos.getX());
      position.putInt("y", pos.getY());
      position.putInt("z", pos.getZ());
      mod.put("position", position);
      mod.put("modifiers", mods);
      modifiers.add(mod);
    });
    nbt.put("modifiers", modifiers);
    return nbt;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
    updateComponents();
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    getFoundComponentsList().stream()
        .filter(c -> c instanceof ISyncableStuff)
        .map(c -> (ISyncableStuff) c)
        .forEach(c -> c.getStuffToSync(container));
  }
}
