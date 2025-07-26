package es.degrassi.mmreborn.common.registration;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.IEventBus;

public class Registration {
  private Registration() {}
  public static final LootContextParamSet MODULAR_MACHINERY_LOOT_PARAMETER_SET = LootContextParamSets.register(
      "modular_machinery_reborn", builder ->
      builder.optional(LootContextParams.ORIGIN).optional(LootContextParams.BLOCK_ENTITY)
  );

  public static void register(final IEventBus bus) {
    DataComponentRegistration.register(bus);
    ComponentRegistration.register(bus);
    MachineHatchTypeRegistration.register(bus);
    DataRegistration.register(bus);
    ProcessorTypeRegistration.register(bus);
    RequirementTypeRegistration.register(bus);
    BlockRegistration.register(bus);
    ItemRegistration.register(bus);
    EntityRegistration.register(bus);
    ContainerRegistration.register(bus);
    RecipeRegistration.register(bus);
    CreativeTabsRegistration.register(bus);
  }
}
