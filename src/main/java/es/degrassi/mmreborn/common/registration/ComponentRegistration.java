package es.degrassi.mmreborn.common.registration;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class ComponentRegistration {
  private ComponentRegistration() {}

  public static final DeferredRegister<ComponentType> MACHINE_COMPONENTS =
      DeferredRegister.create(ComponentType.REGISTRY_KEY, ModularMachineryReborn.MODID);
  public static final Registry<ComponentType> COMPONENTS_REGISTRY = MACHINE_COMPONENTS.makeRegistry(builder -> {});

  public static final Supplier<ComponentType> COMPONENT_ITEM = MACHINE_COMPONENTS.register(rootLC("item"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_DURABILITY= MACHINE_COMPONENTS.register(rootLC("durability"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_DURATION = MACHINE_COMPONENTS.register(rootLC("duration"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_FLUID = MACHINE_COMPONENTS.register(rootLC("fluid"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_ENERGY = MACHINE_COMPONENTS.register(rootLC("energy"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_DIMENSION = MACHINE_COMPONENTS.register(rootLC("dimension"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_BIOME = MACHINE_COMPONENTS.register(rootLC("biome"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_WEATHER = MACHINE_COMPONENTS.register(rootLC("weather"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_TIME = MACHINE_COMPONENTS.register(rootLC("time"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_HEIGHT = MACHINE_COMPONENTS.register(rootLC("height"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_CHUNKLOAD = MACHINE_COMPONENTS.register(rootLC("chunkload"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_EXPERIENCE = MACHINE_COMPONENTS.register(rootLC("experience"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_PARALLEL = MACHINE_COMPONENTS.register(rootLC("parallel"),
      ComponentType::create);
  public static final Supplier<ComponentType> COMPONENT_FUNCTION = MACHINE_COMPONENTS.register(rootLC("function"),
      ComponentType::create);

  public static void register(final IEventBus bus) {
    MACHINE_COMPONENTS.register(bus);
  }
}
