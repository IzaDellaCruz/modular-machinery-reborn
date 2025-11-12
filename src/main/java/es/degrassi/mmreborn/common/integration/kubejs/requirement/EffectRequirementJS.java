package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEffect;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public interface EffectRequirementJS extends RecipeJSBuilder {
  default MachineRecipeBuilderJS giveEffect(ResourceLocation effect, int time, int level, ResourceLocation...filter) {
    List<EntityType<?>> entityFilter = new ArrayList<>();
    for (var type : filter) {
      if (BuiltInRegistries.ENTITY_TYPE.containsKey(type)) {
        entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(type));
      } else {
        return error("Invalid entity ID: {}", type);
      }
    }
    if (BuiltInRegistries.MOB_EFFECT.containsKey(effect)) {
      var requirement = new RequirementEffect(BuiltInRegistries.MOB_EFFECT.getHolder(effect).orElseThrow(), time,
          level, entityFilter);
      return addRequirement(new RecipeRequirement<>(requirement));
    } else {
      return error("Invalid effect ID: {}", effect);
    }
  }

  default MachineRecipeBuilderJS giveEffect(ResourceLocation effect, int time, ResourceLocation...filter) {
    return giveEffect(effect, time, 1, filter);
  }
}
