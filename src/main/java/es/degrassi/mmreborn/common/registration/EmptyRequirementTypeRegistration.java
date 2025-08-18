package es.degrassi.mmreborn.common.registration;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import es.degrassi.mmreborn.common.integration.jei.ingredient.CustomIngredientTypes;
import es.degrassi.mmreborn.common.util.EmptyRequirementType;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Locale;
import java.util.function.Supplier;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class EmptyRequirementTypeRegistration {
  private EmptyRequirementTypeRegistration() {}

  public static final DeferredRegister<EmptyRequirementType> MACHINE_COMPONENTS =
      DeferredRegister.create(EmptyRequirementType.REGISTRY_KEY, ModularMachineryReborn.MODID);

  public static final Registry<EmptyRequirementType> EMPTY_REQUIREMENT_REGISTRY =
      MACHINE_COMPONENTS.makeRegistry(builder -> {
  });

  public static final Supplier<EmptyRequirementType> ITEM =
      MACHINE_COMPONENTS.register(rootLC("item".toLowerCase(Locale.ENGLISH)),
          () -> EmptyRequirementType.create(
              36,
              0,
              16,
              16,
              (component, widgets, recipe) -> {
                widgets.add(component);
              },
              (component, category, builder, recipe, focuses) -> {
                builder.addSlot(component.role(), component.getPosition().x(), component.getPosition().y())
                    .setStandardSlotBackground();
              }
          ));

  public static final Supplier<EmptyRequirementType> FLUID =
      MACHINE_COMPONENTS.register(rootLC("fluid".toLowerCase(Locale.ENGLISH)),
          () -> EmptyRequirementType.create(
              0,
              18,
              16,
              16,
              (component, widgets, recipe) -> {
                widgets.add(component);
              },
              (component, category, builder, recipe, focuses) -> {
                builder.addSlot(component.role(), component.getPosition().x(), component.getPosition().y())
                    .setOverlay(
                        MMRJeiPlugin.jeiHelpers.getGuiHelper().createDrawable(
                            component.texture(),
                            component.getUOffset(),
                            component.getVOffset(),
                            component.getWidth() + 2,
                            component.getHeight() + 2),
                        -1,
                        -1
                    );
              }
          ));

  public static final Supplier<EmptyRequirementType> ENERGY =
      MACHINE_COMPONENTS.register(rootLC("energy".toLowerCase(Locale.ENGLISH)),
          () -> EmptyRequirementType.create(
              18,
              54,
              16,
              52,
              (component, widgets, recipe) -> {
                widgets.add(component);
              },
              (component, category, builder, recipe, focuses) -> {
                builder
                    .addSlot(component.role(), component.getPosition().x(), component.getPosition().y())
                    .setCustomRenderer(CustomIngredientTypes.VOID, component)
                    .addIngredients(CustomIngredientTypes.VOID, component.ingredients());
              }
          ));

  public static void register(final IEventBus bus) {
    MACHINE_COMPONENTS.register(bus);
  }
}
