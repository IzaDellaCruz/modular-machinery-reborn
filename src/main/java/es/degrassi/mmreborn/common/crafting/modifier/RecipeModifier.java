package es.degrassi.mmreborn.common.crafting.modifier;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.codec.RegistrarCodec;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Objects;

@Getter
public abstract class RecipeModifier<
      R extends IRequirement<C, T>,
      C extends MachineComponent<T>,
      T
    > implements IRecipeModifier<R, C, T> {

  public static final NamedCodec<RecipeModifier<?, ?, ?>> CODEC = NamedCodec.record(modifierInstance ->
      modifierInstance.group(
          RegistrarCodec.REQUIREMENT_NEW.fieldOf("requirement").forGetter(RecipeModifier::getRequirementType),
          IOType.CODEC.fieldOf("mode").forGetter(RecipeModifier::getMode),
          OPERATION.CODEC.fieldOf("operation").forGetter(RecipeModifier::getOperation),
          NamedCodec.FLOAT.fieldOf("modifier").forGetter(RecipeModifier::getModifier),
          NamedCodec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(RecipeModifier::getChance),
          NamedCodec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY).forGetter(RecipeModifier::getMax),
          NamedCodec.FLOAT.optionalFieldOf("min", Float.NEGATIVE_INFINITY).forGetter(RecipeModifier::getMin)
      ).apply(modifierInstance, RecipeModifier::create), "Recipe modifier"
  );

  public static RecipeModifier<?, ?, ?> create(RequirementType<?, ?, ?> requirement, IOType mode, OPERATION operation, float modifier, float chance, float max, float min) {
    if(requirement == RequirementTypeRegistration.SPEED.get())
      mode = IOType.INPUT;
      //return new SpeedRecipeModifier(operation, modifier, chance, max, min);
    return switch (operation) {
      case ADDITION -> new AdditionRecipeModifier<>(requirement, mode, modifier, chance, max, min);
      case MULTIPLICATION -> new MultiplicationRecipeModifier<>(requirement, mode, modifier, chance, max, min);
    };
  }

  public static final RandomSource RAND = RandomSource.create();

  public final RequirementType<R, C, T> requirementType;
  public final IOType mode;
  public final float modifier;
  public final float chance;
  public final float max;
  public final float min;
  public final Component tooltip;

  protected RecipeModifier(RequirementType<R, C, T> requirementType, IOType mode, float modifier, float chance, float max, float min) {
    if (RecipeModifierTargetEvent.Blacklist.BLACKLIST.stream().anyMatch(c -> c.equals(requirementType)))
      throw new UnsupportedOperationException("requirement type: " + requirementType.getId() + " is not a valid option for a Recipe Modifier");
    this.requirementType = requirementType;
    this.mode = mode;
    this.modifier = modifier;
    this.chance = chance;
    this.max = max;
    this.min = min;
    this.tooltip = getDefaultTooltip();
  }

  @Override
  public boolean shouldApply(RequirementType<R, C, T> type, IOType mode) {
    return type == this.requirementType
        && mode == this.mode
        && this.chance > RAND.nextDouble();
  }

  public abstract OPERATION getOperation();

  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    ResourceLocation key = ModularMachineryReborn.getRequirementRegistrar().getKey(requirementType);
    if (key == null)
      key = ModularMachineryReborn.rl("speed");
    json.addProperty("target", key.toString());
    json.addProperty("mode", mode.getSerializedName());
    json.addProperty("modifier", modifier);
    json.addProperty("operation", getOperation().toString());
    json.addProperty("chance", chance);
    return json;
  }

  public CompoundTag asTag() {
    CompoundTag tag = new CompoundTag();
    ResourceLocation key = ModularMachineryReborn.getRequirementRegistrar().getKey(requirementType);
    if (key == null)
      key = ModularMachineryReborn.rl("speed");
    tag.putString("target", key.toString());
    tag.putString("mode", mode.getSerializedName());
    tag.putFloat("modifier", modifier);
    tag.putString("operation", getOperation().toString());
    tag.putFloat("chance", chance);
    return tag;
  }

  protected String getTargetValue() {
    return Objects.requireNonNull(ModularMachineryReborn.getRequirementRegistrar().getKey(requirementType)).getPath();
  }
}
