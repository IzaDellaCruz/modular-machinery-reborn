package es.degrassi.mmreborn.common.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.latvian.mods.rhino.type.TypeInfo;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;

import java.util.Collections;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public interface ModularMachineryRebornRecipeSchemas {
  RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new RecipeComponent<>() {
    @Override
    public Codec<ResourceLocation> codec() {
      return ResourceLocation.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
      return TypeInfo.of(ResourceLocation.class);
    }
  };

  RecipeComponent<RecipeRequirement<?, ?>> REQUIREMENT_COMPONENT = new RecipeComponent<>() {
    @Override
    public Codec<RecipeRequirement<?, ?>> codec() {
      return RecipeRequirement.CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
      return TypeInfo.of(RecipeRequirement.class);
    }
  };

  RecipeComponent<List<RecipeRequirement<?, ?>>> REQUIREMENT_LIST = REQUIREMENT_COMPONENT.asList();

  RecipeKey<ResourceLocation> MACHINE_ID = RESOURCE_LOCATION.key("machine", ComponentRole.OTHER);
  RecipeKey<TickDuration> TIME = TimeComponent.TICKS.key("time", ComponentRole.OTHER);
  RecipeKey<Boolean> VOID = BooleanComponent.BOOLEAN.key("voidOnFailure", ComponentRole.OTHER).optional(true).alwaysWrite().exclude();
  RecipeKey<Boolean> HIDE =
      BooleanComponent.BOOLEAN.key("hidden", ComponentRole.OTHER).optional(false).alwaysWrite().exclude();
  RecipeKey<Boolean> SHOULD_RENDER_PROGRESS = BooleanComponent.BOOLEAN.key("renderProgress", ComponentRole.OTHER).optional(true).alwaysWrite().exclude();
  RecipeKey<Integer> PROGRESS_X = NumberComponent.INT.key("progress_x", ComponentRole.OTHER).optional(74).alwaysWrite().exclude();
  RecipeKey<Integer> PROGRESS_Y = NumberComponent.INT.key("progress_y", ComponentRole.OTHER).optional(8).alwaysWrite().exclude();
  RecipeKey<Integer> WIDTH = NumberComponent.INT.key("width", ComponentRole.OTHER).optional(256).alwaysWrite().exclude();
  RecipeKey<Integer> HEIGHT = NumberComponent.INT.key("height", ComponentRole.OTHER).optional(256).alwaysWrite().exclude();
  RecipeKey<List<RecipeRequirement<?, ?>>> REQUIREMENTS = REQUIREMENT_LIST.key("requirements", ComponentRole.OTHER).optional(Collections.emptyList()).allowEmpty().alwaysWrite().exclude();
  RecipeKey<List<RecipeRequirement<?, ?>>> JEI_REQUIREMENTS = REQUIREMENT_LIST.key("jei_requirements", ComponentRole.OTHER).optional(Collections.emptyList()).allowEmpty().alwaysWrite().exclude();
  RecipeKey<Integer> PRIORITY = NumberComponent.INT.key("priority", ComponentRole.OTHER).optional(0).alwaysWrite().exclude();
  RecipeSchema CUSTOM_MACHINE = new RecipeSchema(MACHINE_ID, TIME, PROGRESS_X, PROGRESS_Y, WIDTH, HEIGHT,
      REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, VOID, SHOULD_RENDER_PROGRESS, HIDE).factory(new KubeRecipeFactory(ModularMachineryReborn.rl("recipe"),
      TypeInfo.of(MachineRecipeBuilderJS.class), MachineRecipeBuilderJS::new));
}
