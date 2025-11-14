package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementCheckEntity;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementHealthEntity;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementHealthEntity.Mode;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementKillEntity;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementSpawnEntity;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public interface EntityRequirementJS extends RecipeJSBuilder {
  default MachineRecipeBuilderJS healEntitiesInRadius(int radius, int amount, ResourceLocation... filter) {
    if (radius <= 0) return error("Radius can not be less than 1 for Heal Entity Requirement");
    if (amount <= 0) return error("Amount can not be less than 1 for Heal Entity Requirement");
    List<EntityType<?>> entities = Lists.newArrayList();
    for (var type : filter) {
      try {
        var entity = BuiltInRegistries.ENTITY_TYPE.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, type));
        entities.add(entity);
      } catch(Exception e) {
        return error("Invalid Entity ID: {}", type);
      }
    }
    return addRequirement(new RecipeRequirement<>(new RequirementHealthEntity(amount, radius, Mode.OUTPUT, entities)));
  }

  default MachineRecipeBuilderJS hurtEntitiesInRadius(int radius, int amount, ResourceLocation... filter) {
    if (radius <= 0) return error("Radius can not be less than 1 for Hurt Entity Requirement");
    if (amount <= 0) return error("Amount can not be less than 1 for Hurt Entity Requirement");
    List<EntityType<?>> entities = Lists.newArrayList();
    for (var type : filter) {
      try {
        var entity = BuiltInRegistries.ENTITY_TYPE.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, type));
        entities.add(entity);
      } catch(Exception e) {
        return error("Invalid Entity ID: {}", type);
      }
    }
    return addRequirement(new RecipeRequirement<>(new RequirementHealthEntity(amount, radius, Mode.INPUT, entities)));
  }

  default MachineRecipeBuilderJS hurtEntities(int amount, ResourceLocation...filter) {
    return hurtEntitiesInRadius(1, amount, filter);
  }

  default MachineRecipeBuilderJS healEntities(int amount, ResourceLocation...filter) {
    return healEntitiesInRadius(1, amount, filter);
  }

  default MachineRecipeBuilderJS checkEntitiesAmountInRadius(int radius, int amount,
                                                             boolean whitelist, ResourceLocation...filter) {
    if (radius <= 0) return error("Radius can not be less than 1 for Check Entity Requirement");
    if (amount <= 0) return error("Amount can not be less than 1 for Check Entity Requirement");
    List<EntityType<?>> entities = Lists.newArrayList();
    for (var type : filter) {
      try {
        var entity = BuiltInRegistries.ENTITY_TYPE.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, type));
        entities.add(entity);
      } catch(Exception e) {
        return error("Invalid Entity ID: {}", type);
      }
    }
    return addRequirement(new RecipeRequirement<>(new RequirementCheckEntity(amount, radius,
        RequirementCheckEntity.CheckAction.AMOUNT, entities, whitelist)));
  }

  default MachineRecipeBuilderJS checkEntitiesAmount(int amount, boolean whitelist, ResourceLocation... filter) {
    return checkEntitiesAmountInRadius(1, amount, whitelist, filter);
  }

  default MachineRecipeBuilderJS checkEntitiesHealthInRadius(int radius, int amount, boolean whitelist,
                                                             ResourceLocation...filter) {
    if (radius <= 0) return error("Radius can not be less than 1 for Check Entity Requirement");
    if (amount <= 0) return error("Amount can not be less than 1 for Check Entity Requirement");
    List<EntityType<?>> entities = Lists.newArrayList();
    for (var type : filter) {
      try {
        var entity = BuiltInRegistries.ENTITY_TYPE.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, type));
        entities.add(entity);
      } catch(Exception e) {
        return error("Invalid Entity ID: {}", type);
      }
    }
    return addRequirement(new RecipeRequirement<>(new RequirementCheckEntity(amount, radius,
        RequirementCheckEntity.CheckAction.HEALTH, entities, whitelist)));
  }

  default MachineRecipeBuilderJS checkEntitiesHealth(int amount, boolean whitelist, ResourceLocation...filter) {
    return checkEntitiesHealthInRadius(1, amount, whitelist, filter);
  }

  default MachineRecipeBuilderJS killEntitiesInRadius(int radius, int amount, ResourceLocation...filter) {
    if (radius <= 0) return error("Radius can not be less than 1 for Kill Entity Requirement");
    if (amount <= 0) return error("Amount can not be less than 1 for Kill Entity Requirement");
    List<EntityType<?>> entities = Lists.newArrayList();
    for (var type : filter) {
      try {
        var entity = BuiltInRegistries.ENTITY_TYPE.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, type));
        entities.add(entity);
      } catch(Exception e) {
        return error("Invalid Entity ID: {}", type);
      }
    }
    return addRequirement(new RecipeRequirement<>(new RequirementKillEntity(amount, radius, entities)));
  }

  default MachineRecipeBuilderJS killEntities(int amount, ResourceLocation...filter) {
    return killEntitiesInRadius(1, amount, filter);
  }

  default  MachineRecipeBuilderJS killEntity(ResourceLocation...filter) {
    return killEntities(1, filter);
  }

  default MachineRecipeBuilderJS spawnEntitiesInRadius(int radius, int amount, ResourceLocation type) {
    if (radius <= 0) return error("Radius can not be less than 1 for Spawn Entity Requirement");
    if (amount <= 0) return error("Amount can not be less than 1 for Spawn Entity Requirement");
    try {
      var entity = BuiltInRegistries.ENTITY_TYPE.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, type));
      return addRequirement(new RecipeRequirement<>(new RequirementSpawnEntity(amount, radius, entity)));
    } catch(Exception e) {
      return error("Invalid Entity ID: {}", type);
    }
  }

  default MachineRecipeBuilderJS spawnEntities(int amount, ResourceLocation type) {
    return spawnEntitiesInRadius(1, amount, type);
  }

  default MachineRecipeBuilderJS spawnEntity(ResourceLocation type) {
    return spawnEntities(1, type);
  }
}
