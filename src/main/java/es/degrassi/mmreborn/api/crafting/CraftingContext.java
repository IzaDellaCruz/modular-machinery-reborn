package es.degrassi.mmreborn.api.crafting;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.modifier.RecipeModifier;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.function.Supplier;

public class CraftingContext implements ICraftingContext {

  private final MachineControllerEntity tile;
  private final RecipeHolder<? extends MachineRecipe> recipe;
  private final Supplier<Float> progressTimeGetter;
  private final int core;
  private float baseSpeed = 1.0f;

  public CraftingContext(MachineControllerEntity tile, RecipeHolder<? extends MachineRecipe> recipe, Supplier<Float> progressTimeGetter, int core) {
    this.tile = tile;
    this.recipe = recipe;
    this.progressTimeGetter = progressTimeGetter;
    this.core = core;
  }

  @Override
  public MachineControllerEntity getMachineTile() {
    return this.tile;
  }

  @Override
  public int getCurrentCore() {
    return this.core;
  }

  @Override
  public MachineRecipe getRecipe() {
    return this.recipe.value();
  }

  @Override
  public ResourceLocation getRecipeId() {
    return this.recipe.id();
  }

  @Override
  public float getRemainingTime() {
    if(getRecipe() == null)
      return 0;
    return getRecipe().getRecipeTotalTickTime() - this.progressTimeGetter.get();
  }

  @Override
  public float getBaseSpeed() {
    return this.baseSpeed;
  }

  @Override
  public void setBaseSpeed(float baseSpeed) {
    this.baseSpeed = baseSpeed;
  }

  @Override
  public float getModifiedSpeed() {
    if(getRecipe() == null)
      return this.baseSpeed;
    int baseTime = getRecipe().getRecipeTotalTickTime();
    float modifiedTime = getModifiedValue(baseTime, RequirementTypeRegistration.SPEED.get(), IOType.INPUT);
    float speed = baseTime * this.baseSpeed / modifiedTime;
    return Math.max(0.01f, speed);
  }

  @Override
  public long getIntegerModifiedValue(float value, IRequirement<?, ?> requirement) {
    return Math.round(getModifiedValue(value, requirement));
  }

  @Override
  public long getPerTickIntegerModifiedValue(float value, IRequirement<?, ?> requirement) {
    return Math.round(getPerTickModifiedValue(value, requirement));
  }

  @Override
  public List<RecipeModifier> getModifiers(RequirementType<?, ?> target) {
    return tile.getComponentManager().getModifiers(target);
  }

  @Override
  public float getModifiedValue(float value, IRequirement<?, ?> requirement) {
    return getModifiedValue(value, requirement.getType(), requirement.getMode());
  }

  @Override
  public float getPerTickModifiedValue(float value, IRequirement<?, ?> requirement) {
    if(this.getRemainingTime() > 0)
      return getModifiedValue(value, requirement) * Math.min(this.getModifiedSpeed(), this.getRemainingTime());
    return getModifiedValue(value, requirement) * this.getModifiedSpeed();
  }

  private float getModifiedValue(float value, RequirementType<?, ?> type, IOType mode) {
    float modified = value;
    var modifiers = tile.getComponentManager().getModifiers(type);
    for (var modifier : modifiers) {
      if (modifier.shouldApply(type, mode)) {
        modified = modifier.apply(modified);
      }
    }
    return modified;
  }

  public static class Mutable extends CraftingContext {

    private MachineRecipe recipe;
    private ResourceLocation recipeId;

    public Mutable(MachineControllerEntity tile, int core) {
      super(tile, null, () -> 0.0f, core);
    }

    public Mutable setRecipe(MachineRecipe recipe, ResourceLocation recipeId) {
      this.recipe = recipe;
      this.recipeId = recipeId;
      return this;
    }

    @Override
    public MachineRecipe getRecipe() {
      return this.recipe;
    }

    @Override
    public ResourceLocation getRecipeId() {
      return this.recipeId;
    }
  }
}
