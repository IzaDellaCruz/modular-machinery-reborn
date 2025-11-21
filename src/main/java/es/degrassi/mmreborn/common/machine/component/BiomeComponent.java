package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.entity.BiomeReaderEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BiomeComponent extends MachineComponent<List<ResourceLocation>> {
  private final BiomeReaderEntity entity;

  public BiomeComponent(BiomeReaderEntity entity) {
    super(IOType.INPUT);
    this.entity = entity;
  }

  @Override
  public ComponentType<List<ResourceLocation>> getComponentType() {
    return ComponentRegistration.COMPONENT_BIOME.get();
  }

  @Override
  public List<ResourceLocation> getContainerProvider() {
    return List.of(entity.getLevel().getBiome(entity.getBlockPos()).unwrapKey().get().location());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<List<ResourceLocation>>> C merge(C c) {
    return (C) this;
  }
}
