package es.degrassi.mmreborn.common.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.controller.ControllerAccessible;
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
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ComponentManager implements INBTSerializable<CompoundTag>, ISyncableStuff {
  @Getter
  private final MachineControllerEntity controller;

  private final Map<BlockPos, MachineComponent<?>> foundComponents = Maps.newHashMap();
  private final Map<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> foundComponentsValues = Maps.newHashMap();
  private final Map<BlockPos, List<ModifierReplacement>> foundModifiers = Maps.newHashMap();
  private final Map<RequirementType<?, ?, ?>, List<RecipeModifier<?, ?, ?>>> foundModifiersValues = Maps.newHashMap();

  @Getter
  @Accessors(makeFinal = true)
  private final Set<BlockPos> cachedBlocks = Sets.newHashSet();

  public ComponentManager(MachineControllerEntity entity) {
    this.controller = entity;
  }

  public final void reset() {
    cachedBlocks.forEach(block -> {
      if (controller.getLevel().getBlockEntity(block) instanceof ColorableMachineComponentEntity entity) {
        entity.getControllerPosSet().remove(controller.getBlockPos());
        entity.setMachineColor(Config.machineColor);
        if (entity instanceof TextureableMachineEntity e) e.resetTextures();
        entity.setChanged();
      }
    });
    foundComponents.clear();
    foundModifiers.clear();
    foundComponentsValues.clear();
    foundModifiersValues.clear();
  }

  public final void updateModifiers() {
    if (controller.getFoundMachine() == DynamicMachine.DUMMY) return;
    Level level = controller.getLevel();
    if (level == null) return;
    foundModifiers.clear();
    foundModifiers.putAll(gatherModifiers());
    foundModifiersValues.clear();
    foundModifiersValues.putAll(filterModifiers());
    controller.setChanged();
  }

  public final void updateComponents() {
    if (controller.getFoundMachine() == DynamicMachine.DUMMY)
      return;
    Level level = controller.getLevel();
    if (level == null) return;
    reset();
    foundComponents.putAll(gatherComponents());

    if (controller.getModelData().get(ControllerBakedModel.DATA).hasCustomModel()) {
      controller.getLevel().setBlockAndUpdate(controller.getBlockPos(), controller.getBlockState().setValue(BlockMachineComponent.CONNECT_TEXTURES, false));
    } else if(!controller.getBlockState().getValue(BlockMachineComponent.CONNECT_TEXTURES)) {
      controller.getLevel().setBlockAndUpdate(controller.getBlockPos(), controller.getBlockState().setValue(BlockMachineComponent.CONNECT_TEXTURES, true));
    }
    foundComponents.forEach((pos, comp) -> {
      var oldState = controller.getLevel().getBlockState(pos);
      var entity = controller.getLevel().getBlockEntity(pos);
      if (!(entity instanceof TextureableMachineEntity)) return;
      var data = entity.getModelData();
      if (!data.has(HatchBakedModel.TEXTURE_DATA)) return;
      var state = oldState.setValue(BlockMachineComponent.CONNECT_TEXTURES,
          data.get(HatchBakedModel.TEXTURE_DATA).hasDefaultTextures());
      controller.getLevel().setBlockAndUpdate(pos, state);
    });
    foundComponentsValues.putAll(filter());
    updateModifiers();
    controller.getProcessor().setMachineInventoryChanged();
    controller.setChanged();
  }

  private Map<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> filter() {
    Map<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> foundComponentsValues = Maps.newHashMap();
    for (MachineComponent<?> comp : foundComponents.values()) {
      foundComponentsValues
          .computeIfAbsent(comp.getComponentType(), t -> Maps.newHashMap())
          .computeIfAbsent(comp.getIOType(), io -> Lists.newArrayList())
          .add(comp);
    }
    return foundComponentsValues;
  }

  private Map<RequirementType<?, ?, ?>, List<RecipeModifier<?, ?, ?>>> filterModifiers() {
    Map<RequirementType<?, ?, ?>, List<RecipeModifier<?, ?, ?>>> foundModifiersValues = Maps.newHashMap();
    for (var modifierList : foundModifiers.values()) {
      modifierList.stream()
          .map(ModifierReplacement::getModifiers)
          .flatMap(List::stream)
          .forEach(modifier -> {
            foundModifiersValues.computeIfAbsent(modifier.getRequirementType(), t -> Lists.newArrayList())
                .add(modifier);
          });
    }
    return foundModifiersValues;
  }

  public final List<MachineComponent<?>> getFoundComponentsList() {
    if (foundComponents.isEmpty()) updateComponents();
    return foundComponents.values()
        .stream()
        .toList();
  }

  public List<ModifierReplacement> getFoundModifiersList() {
    if (foundModifiers.isEmpty()) updateComponents();
    return foundModifiers.values().stream().flatMap(List::stream).toList();
  }

  public final Map<BlockPos, MachineComponent<?>> getFoundComponentsMap() {
    return foundComponents;
  }

  public final Map<BlockPos, List<ModifierReplacement>> getFoundModifiersMap() {
    return foundModifiers;
  }

  private Map<BlockPos, MachineComponent<?>> gatherComponents() {
    Map<BlockPos, MachineComponent<?>> map = Maps.newHashMap();
    Map<BlockPos, BlockIngredient> filteredMap = controller.getFoundMachine().getPattern().getBlocksFiltered(controller.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    BlockPos controllerPos = controller.getBlockPos();
    Level level = controller.getLevel();
    if (level == null) return map;
    for (BlockPos potentialPosition : filteredMap.keySet()) {
      BlockPos realPos = controllerPos.offset(potentialPosition);
      if (!realPos.equals(controllerPos))
        cachedBlocks.add(realPos);
      BlockEntity te = level.getBlockEntity(realPos);
      if (te instanceof ColorableMachineComponentEntity entity) {
        entity.getControllerPosSet().add(controllerPos);
      }
      if (te instanceof MachineComponentEntity<?> entity) {
        var component = entity.provideComponent();
        if (entity instanceof ControllerAccessible accessible) {
          if (accessible.getControllerPos() == null)
            accessible.setControllerPos(controllerPos.immutable());
          if (component != null && controllerPos.equals(accessible.getControllerPos()))
            map.put(realPos, component);
        } else {
          map.put(realPos, component);
        }
      }
    }
    map.put(controllerPos, new FunctionComponent(controllerPos));
    return map;
  }

  private Map<BlockPos, List<ModifierReplacement>> gatherModifiers() {
    Map<BlockPos, List<ModifierReplacement>> map = Maps.newHashMap();
    if (controller.getLevel() == null) return map;
    controller.getFoundMachine()
        .getPattern()
        .getPattern()
        .getModifiers(controller.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
        .forEach((potentialPosition, modifiers) -> {
          BlockPos realPos = controller.getBlockPos().offset(potentialPosition);
          BlockInWorld biw = new BlockInWorld(controller.getLevel(), realPos, false);
          if (modifiers.stream().anyMatch(modifier -> modifier.getIngredient().getAll().stream().anyMatch(state -> state.test(biw))))
            map.put(realPos, modifiers);
        });
    return map;
  }

  @SuppressWarnings("unchecked")
  public <R extends IRequirement<C, T>, C extends MachineComponent<T>, T> List<RecipeModifier<R, C, T>> getModifiers(RequirementType<R, C, T> type) {
    if (foundModifiersValues.isEmpty() && !getController().getFoundMachine().getModifiers().isEmpty()) updateModifiers();
    return Optional.ofNullable(foundModifiersValues.get(type))
        .orElse(List.of())
        .stream()
        .map(r -> (RecipeModifier<R, C, T>) r)
        .toList();
  }

  public <C extends MachineComponent<T>, T> Optional<C> getComponent(IRequirement<C, T> requirement, ICraftingContext context) {
    if (foundComponentsValues.isEmpty()) updateComponents();
    if (requirement.getType().equals(RequirementTypeRegistration.DURABILITY.get())) {
      return getComponent(requirement.getComponentType(), IOType.INPUT);
    }
    return getComponent(requirement.getComponentType(), requirement.getMode());
  }

  public Optional<ParallelComponent> getParallel() {
    return getComponent(ComponentRegistration.COMPONENT_PARALLEL.get(), IOType.INPUT);
  }

  public Optional<ItemComponent> getItemComponent(IOType mode) {
    return getComponent(ComponentRegistration.COMPONENT_ITEM.get(), mode);
  }

  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<T>, T> Optional<C> getComponent(ComponentType<T> type, IOType mode) {
    if (foundComponentsValues.isEmpty()) updateComponents();
    return Optional.ofNullable(foundComponentsValues.get(type))
        .map(m -> m.get(mode))
        .stream()
        .flatMap(List::stream)
        .map(m -> (C) m)
        .filter(Objects::nonNull)
        .sorted()
        .reduce((c1, c2) -> {
          if (c1.canMerge(c2)) return c1.merge(c2);
          return c1;
        });
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();
    CompoundTag componentsByType = new CompoundTag();
    foundComponentsValues.forEach((type, map) -> {
      CompoundTag listByMode = new CompoundTag();
      map.forEach((mode, list) -> listByMode.put(
          mode.getSerializedName(),
          getComponent(type, mode).map(component -> component.asTag(provider)).orElse(new CompoundTag())
      ));
      componentsByType.put(type.getId().toString(), listByMode);
    });
    nbt.put("components", componentsByType);
    ListTag modifiers = new ListTag();
    foundModifiers
        .forEach((pos, list) -> {
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
