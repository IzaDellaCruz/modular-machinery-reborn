package es.degrassi.mmreborn.api.crafting.requirement;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class RecipeRequirement<C extends MachineComponent<?>, R extends IRequirement<C>> {
  public static final NamedCodec<RecipeRequirement<?, ?>> CODEC = NamedCodec.record(recipeRequirementInstance ->
      recipeRequirementInstance.group(
          IRequirement.CODEC.forGetter(RecipeRequirement::requirement),
          NamedCodec.floatRange(0.0f, 1.0f).optionalFieldOf("chance", 1.0f).forGetter(requirement -> requirement.chance)
      ).apply(recipeRequirementInstance, RecipeRequirement::new), "Recipe requirement"
  );

  private final R requirement;
  private float chance;

  public RecipeRequirement(R requirement, float chance) {
    this.requirement = requirement;
    this.chance = chance;
  }

  @SuppressWarnings("unchecked")
  public RecipeRequirement<C, R> castRequirement(RecipeRequirement<?, ?> requirement) {
    return (RecipeRequirement<C, R>) requirement;
  }

  public RecipeRequirement(R requirement) {
    this(requirement, 1.0f);
  }

  @SuppressWarnings("unchecked")
  public RequirementType<R> getType() {
    return (RequirementType<R>) this.requirement.getType();
  }

  public R requirement() {
    return this.requirement;
  }

  public float chance() {
    return this.chance;
  }

  public void setChance(float chance) {
    this.chance = Mth.clamp(chance, 0.0f, 1.0f);
  }

  public C findComponent(ComponentManager manager, ICraftingContext context) {
    return manager.getComponent(this.requirement, context).orElse(null);
  }

  public CraftingResult test(ComponentManager manager, ICraftingContext context) {
    C component = findComponent(manager, context);
    if (component == null) return CraftingResult.error(requirement.getMissingComponentErrorMessage(requirement.getMode()));
    return this.requirement.test(component, context) ? CraftingResult.success() : CraftingResult.error(Component.empty());
  }

  public boolean shouldSkip(RandomSource rand, ICraftingContext context) {
    float chance = context.getModifiedValue(this.chance, this.requirement);
    return rand.nextFloat() > chance;
  }

  public boolean isModified() {
    return requirement().isModified();
  }

  public JsonObject asJson() {
    JsonObject json = requirement.asJson();
    json.addProperty("chance", chance);
    return json;
  }
}
