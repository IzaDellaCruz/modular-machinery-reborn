package es.degrassi.mmreborn.common.integration.kubejs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.postprocessing.RecipePostProcessorTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.latvian.mods.rhino.type.TypeInfo;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.common.integration.kubejs.builder.FuelDataJS;
import es.degrassi.mmreborn.common.integration.kubejs.builder.MachineBuilderJS.MachineKubeEvent;
import es.degrassi.mmreborn.common.integration.kubejs.builder.ModifierBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.builder.ProgressDataJS;
import es.degrassi.mmreborn.common.integration.kubejs.builder.StructureBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.function.FunctionKubeEvent;
import es.degrassi.mmreborn.common.integration.kubejs.function.MachineControllerJS;
import es.degrassi.mmreborn.common.util.sound.Sounds;
import es.degrassi.mmreborn.common.util.IntRange;
import es.degrassi.mmreborn.common.util.MachineModelLocation;
import net.minecraft.network.chat.Component;

public class MMRKubeJSPlugin implements KubeJSPlugin {
  public static final EventGroup MMR_EVENTS = EventGroup.of("MMREvents");
  public static final EventHandler MACHINES = MMR_EVENTS.server("machines", () -> MachineKubeEvent.class);
  public static final TargetedEventHandler<String> FUNCTIONS = MMR_EVENTS
      .server("recipeFunction", () -> FunctionKubeEvent.class)
      .hasResult(TypeInfo.of(Component.class))
      .requiredTarget(EventTargetType.STRING);

  @Override
  public void registerEvents(EventGroupRegistry registry) {
    registry.register(MMR_EVENTS);
  }

  @Override
  public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
    registry.register(ModularMachineryRebornRecipeComponents.RESOURCE_LOCATION);
    registry.register(ModularMachineryRebornRecipeComponents.REQUIREMENT_COMPONENT);
    registry.register(ModularMachineryRebornRecipeComponents.PROGRESS_DATA);
  }

  @Override
  public void registerRecipeFactories(RecipeFactoryRegistry registry) {
    registry.register(ModularMachineryReborn.rl("machine_recipe"), MachineRecipeBuilderJS.class, MachineRecipeBuilderJS::new);
  }

  @Override
  public void registerRecipePostProcessors(RecipePostProcessorTypeRegistry registry) {
    registry.register(RecipeIdPostProcessor.TYPE);
  }

  @Override
  public void beforeScriptsLoaded(ScriptManager manager) {
    RecipeIdPostProcessor.IDS.clear();
  }

  @Override
  public void registerBindings(BindingRegistry registry) {
    registry.add("MMRStructureBuilder", StructureBuilderJS.class);
    registry.add("MMRModifierReplacement", ModifierBuilderJS.class);
    registry.add("MMRRecipeModifier", ModifierBuilderJS.RecipeModifierBuilderJS.class);
    registry.add("ControllerModel", MachineModelLocation.class);
    registry.add("MachineController", MachineControllerJS.class);
    registry.add("ProgressData", ProgressDataJS.class);
    registry.add("FuelData", FuelDataJS.class);
  }

  @Override
  public void registerTypeWrappers(TypeWrapperRegistry registry) {
    registry.register(IntRange.class, IntRange::of);
    registry.registerCodec(Sounds.class, Sounds.CODEC.codec());
    registry.register(BlockIngredient.class, o -> {
      try {
          return BlockIngredient.create(o);
        } catch (CommandSyntaxException e) {
          throw new IllegalArgumentException("Error parsing block ingredient: " + o + "\n" + e.getMessage());
        }
    });
    registry.register(PartialBlockState.class, o -> {
      if(o instanceof CharSequence charSequence) {
        try {
          return PartialBlockState.of(charSequence.toString());
        } catch (CommandSyntaxException e) {
          throw new IllegalArgumentException("Error parsing BlockState: " + o + "\n" + e.getMessage());
        }
      }
      throw new IllegalArgumentException("BlockState must be a string");
    });
  }
}
