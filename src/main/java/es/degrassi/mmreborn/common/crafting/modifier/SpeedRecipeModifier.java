package es.degrassi.mmreborn.common.crafting.modifier;

import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SpeedRecipeModifier extends RecipeModifier {

  private final OPERATION operation;

  public SpeedRecipeModifier(OPERATION operation, float modifier, float chance, float max, float min) {
    super(RequirementTypeRegistration.SPEED.get(), IOType.INPUT, modifier, chance, max, min);
    this.operation = operation;
  }

  @Override
  public boolean shouldApply(RequirementType<?, ?> type, IOType mode) {
    return type == this.requirementType
        && this.chance > RAND.nextDouble();
  }

  @Override
  public float apply(float original) {
    float modified = switch (this.operation) {
      case ADDITION -> original + this.modifier;
      case MULTIPLICATION -> original * this.modifier;
    };
    return Mth.clamp(modified, this.min, this.max);
  }

  @Override
  public Component getDefaultTooltip() {
    if (requirementType == RequirementTypeRegistration.SPEED.get()
        || requirementType == RequirementTypeRegistration.LOOT_TABLE.get()
        || requirementType == RequirementTypeRegistration.DURABILITY.get()
    )
      return Component.translatable("mmr.recipe.modifier." +  getTargetValue() + "." + operation, modifier);
    return Component.translatable("mmr.recipe.modifier." + getTargetValue() + "." + operation, modifier,
        getMode().getSerializedName(), chance);
  }

  @Override
  public OPERATION getOperation() {
    return this.operation;
  }
}
