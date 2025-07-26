package es.degrassi.mmreborn.common.registration;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class CreativeTabsRegistration {
  private CreativeTabsRegistration() {}
  public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularMachineryReborn.MODID);

  public static final Supplier<CreativeModeTab> MODULAR_MACHINERY_REBORN_TAB = CREATIVE_TABS.register("modular_machinery_reborn", () -> CreativeModeTab.builder()
    .title(Component.translatable(rootLC("itemGroup.modular_machinery_reborn.group")))
    .icon(ItemRegistration.MODULARIUM.get()::getDefaultInstance)
    .displayItems((params, output) -> {
      ItemRegistration.ITEMS.getEntries().forEach(entry -> {
        if (entry.get() instanceof ControllerItem) {
          ModularMachineryReborn.MACHINES.keySet().forEach(id -> {
            if (id.equals(DynamicMachine.DUMMY.getRegistryName())) return;
            output.accept(ControllerItem.makeMachineItem(id));
          });
        } else
          output.accept(entry.get());
      });
    }).build()
  );

  public static void register(final IEventBus bus) {
    CREATIVE_TABS.register(bus);
  }
}
