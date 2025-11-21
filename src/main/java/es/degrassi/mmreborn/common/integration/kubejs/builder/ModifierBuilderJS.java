package es.degrassi.mmreborn.common.integration.kubejs.builder;

import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.common.crafting.modifier.AdditionRecipeModifier;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.crafting.modifier.MultiplicationRecipeModifier;
import es.degrassi.mmreborn.common.crafting.modifier.RecipeModifier;
import es.degrassi.mmreborn.common.crafting.modifier.IRecipeModifier.OPERATION;
import es.degrassi.mmreborn.common.crafting.modifier.SpeedRecipeModifier;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.google.common.collect.Lists;

import java.util.List;

public class ModifierBuilderJS {
  private BlockIngredient ingredient;
  private final List<RecipeModifier<?, ?, ?>> modifiers = Lists.newArrayList();
  private BlockPos position;

  @HideFromJS
  private ModifierBuilderJS() {}

  public static ModifierBuilderJS create() {
    return new ModifierBuilderJS();
  }

  public ModifierBuilderJS ingredient(BlockIngredient ingredient) {
    this.ingredient = ingredient;
    return this;
  }

  /**
   *
   * @param x relative to X position of controller
   * @param y relative to Y position of controller
   * @param z relative to Z position of controller
   * @return position relative to controller
   */
  public ModifierBuilderJS position(int x, int y, int z) {
    this.position = new BlockPos(x, y, z);
    return this;
  }

  public ModifierBuilderJS addModifier(RecipeModifierBuilderJS modifier) {
    this.modifiers.add(modifier.build());
    return this;
  }

  public ModifierReplacement build() {
    return new ModifierReplacement(ingredient, modifiers, position);
  }

  public static class RecipeModifierBuilderJS {
    private RequirementType<?, ?, ?> target = RequirementTypeRegistration.SPEED.get();
    private IOType mode = IOType.INPUT;
    private float modifier;
    private OPERATION operation = OPERATION.ADDITION;
    private float chance = 1F;
    private float min = Float.NEGATIVE_INFINITY;
    private float max = Float.POSITIVE_INFINITY;

    @HideFromJS
    private RecipeModifierBuilderJS() {}

    public static RecipeModifierBuilderJS create() {
      return new RecipeModifierBuilderJS();
    }

    public RecipeModifierBuilderJS target(ResourceLocation target) {
      this.target = ModularMachineryReborn.getRequirementRegistrar().get(target);
      if (this.target == null) throw new IllegalArgumentException("Invalid recipe target");
      if (RecipeModifier.blacklist.stream().map(RequirementType::getId).anyMatch(target::equals))
        throw new IllegalArgumentException("This type is not allowed as recipe modifier type");
      return this;
    }

    public RecipeModifierBuilderJS addition() {
      this.operation = OPERATION.ADDITION;
      return this;
    }

    public RecipeModifierBuilderJS multiply() {
      this.operation = OPERATION.MULTIPLICATION;
      return this;
    }

    public RecipeModifierBuilderJS input() {
      this.mode = IOType.INPUT;
      return this;
    }

    public RecipeModifierBuilderJS output() {
      this.mode = IOType.OUTPUT;
      return this;
    }

    public RecipeModifierBuilderJS modifier(float modifier) {
      this.modifier = modifier;
      return this;
    }

    public RecipeModifierBuilderJS chance(float chance) {
      this.chance = chance;
      return this;
    }

    public RecipeModifierBuilderJS min(float min) {
      this.min = min;
      return this;
    }

    public RecipeModifierBuilderJS max(float max) {
      this.max = max;
      return this;
    }

    public RecipeModifier<?, ?, ?> build() {
      if (target == RequirementTypeRegistration.SPEED.get())
        return new SpeedRecipeModifier(operation, modifier, chance, max, min);
      return switch (operation) {
        case ADDITION -> new AdditionRecipeModifier<>(target, mode, modifier, chance, max, min);
        case MULTIPLICATION -> new MultiplicationRecipeModifier<>(target, mode, modifier, chance, max, min);
      };
    }
  }
}
