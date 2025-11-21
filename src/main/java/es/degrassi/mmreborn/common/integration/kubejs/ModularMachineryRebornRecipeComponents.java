package es.degrassi.mmreborn.common.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;

import es.degrassi.mmreborn.common.crafting.helper.ProgressData;
import net.minecraft.resources.ResourceLocation;

public interface ModularMachineryRebornRecipeComponents {
  RecipeComponentType<ResourceLocation> RESOURCE_LOCATION =
      RecipeComponentType.unit(ModularMachineryReborn.rl("rl"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<ResourceLocation> type() {
          return RESOURCE_LOCATION;
        }

        @Override
    public Codec<ResourceLocation> codec() {
      return ResourceLocation.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
      return TypeInfo.of(ResourceLocation.class);
    }
  });

  RecipeComponentType<RecipeRequirement<?, ?, ?>> REQUIREMENT_COMPONENT =
      RecipeComponentType.unit(ModularMachineryReborn.rl("requirements"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<?> type() {
          return REQUIREMENT_COMPONENT;
        }

        @Override
    public Codec<RecipeRequirement<?, ?, ?>> codec() {
      return RecipeRequirement.CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
      return TypeInfo.of(RecipeRequirement.class);
    }
  });

  RecipeComponentType<ProgressData> PROGRESS_DATA =
      RecipeComponentType.unit(ModularMachineryReborn.rl("progress_data"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<?> type() {
          return PROGRESS_DATA;
        }

        @Override
        public Codec<ProgressData> codec() {
          return ProgressData.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
          return TypeInfo.of(ProgressData.class);
        }
      });
}
