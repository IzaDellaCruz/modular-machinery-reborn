package es.degrassi.mmreborn.common.registration;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.client.container.EnergyHatchContainer;
import es.degrassi.mmreborn.client.container.ExperienceHatchContainer;
import es.degrassi.mmreborn.client.container.FluidHatchContainer;
import es.degrassi.mmreborn.client.container.ItemBusContainer;
import es.degrassi.mmreborn.client.container.ItemDurabilityContainer;
import es.degrassi.mmreborn.client.container.ParallelHatchContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class ContainerRegistration {
  private ContainerRegistration() {}
  public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, ModularMachineryReborn.MODID);

  public static final DeferredHolder<MenuType<?>, MenuType<ControllerContainer>> CONTROLLER =
      CONTAINERS.register(rootLC("controller"), () -> IMenuTypeExtension.create(ControllerContainer::new));
  public static final DeferredHolder<MenuType<?>, MenuType<EnergyHatchContainer>> ENERGY_HATCH =
      CONTAINERS.register(rootLC("energy_hatch"), () -> IMenuTypeExtension.create(EnergyHatchContainer::new));
  public static final DeferredHolder<MenuType<?>, MenuType<FluidHatchContainer>> FLUID_HATCH =
      CONTAINERS.register(rootLC("fluid_hatch"), () -> IMenuTypeExtension.create(FluidHatchContainer::new));
  public static final DeferredHolder<MenuType<?>, MenuType<ItemBusContainer>> ITEM_BUS =
      CONTAINERS.register(rootLC("item_bus"), () -> IMenuTypeExtension.create(ItemBusContainer::new));
  public static final DeferredHolder<MenuType<?>, MenuType<ItemDurabilityContainer>> ITEM_DURABILITY_HATCH =
      CONTAINERS.register(rootLC("item_durability_hatch"), () -> IMenuTypeExtension.create(ItemDurabilityContainer::new));
  public static final DeferredHolder<MenuType<?>, MenuType<ExperienceHatchContainer>> EXPERIENCE_HATCH =
      CONTAINERS.register(rootLC("experience_hatch"), () -> IMenuTypeExtension.create(ExperienceHatchContainer::new));
  public static final DeferredHolder<MenuType<?>, MenuType<ParallelHatchContainer>> PARALLEL_HATCH =
      CONTAINERS.register(rootLC("parallel_hatch"), () -> IMenuTypeExtension.create(ParallelHatchContainer::new));

  public static void register(IEventBus bus) {
    CONTAINERS.register(bus);
  }
}
