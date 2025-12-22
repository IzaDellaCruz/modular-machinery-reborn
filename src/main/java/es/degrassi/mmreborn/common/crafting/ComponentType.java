package es.degrassi.mmreborn.common.crafting;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ComponentType<T> {
  public static final ResourceKey<Registry<ComponentType<?>>> REGISTRY_KEY =
      ResourceKey.createRegistryKey(ModularMachineryReborn.rl("component_type"));

  protected ComponentType() {}

  public static <T> ComponentType<T> create() {
    return new ComponentType<>();
  }

  public ResourceLocation getId() {
    return ModularMachineryReborn.getComponentRegistrar().getKey(this);
  }
}
