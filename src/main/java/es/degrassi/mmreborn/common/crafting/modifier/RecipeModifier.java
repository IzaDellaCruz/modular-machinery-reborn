package es.degrassi.mmreborn.common.crafting.modifier;

import com.google.common.collect.Lists;
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

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
public abstract class RecipeModifier<
      R extends IRequirement<C, T>,
      C extends MachineComponent<T>,
      T
    > implements IRecipeModifier<R, C, T> {

  public static final NamedCodec<RecipeModifier<?, ?, ?>> CODEC = NamedCodec.record(energyModifierInstance ->
      energyModifierInstance.group(
          RegistrarCodec.REQUIREMENT_NEW.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
          IOType.CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
          OPERATION.CODEC.fieldOf("operation").forGetter(RecipeModifier::getOperation),
          NamedCodec.FLOAT.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
          NamedCodec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(modifier -> modifier.chance),
          NamedCodec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
          NamedCodec.FLOAT.optionalFieldOf("min", Float.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min)
      ).apply(energyModifierInstance, (requirement, mode, operation, modifier, chance, max, min) -> {
        if(requirement == RequirementTypeRegistration.SPEED.get())
          return new SpeedRecipeModifier(operation, modifier, chance, max, min);
        return switch (operation) {
          case ADDITION -> new AdditionRecipeModifier<>(requirement, mode, modifier, chance, max, min);
          case MULTIPLICATION -> new MultiplicationRecipeModifier<>(requirement, mode, modifier, chance, max, min);
        };
      }), "Recipe modifier"
  );

  public static final List<Supplier<RequirementType<?, ?, ?>>> blacklist = Lists.newArrayList();

  static {
    addToBlacklist(RequirementTypeRegistration.DIMENSION);
    addToBlacklist(RequirementTypeRegistration.BIOME);
    addToBlacklist(RequirementTypeRegistration.WEATHER);
    addToBlacklist(RequirementTypeRegistration.TIME);
    addToBlacklist(RequirementTypeRegistration.CHUNKLOAD);
    addToBlacklist(RequirementTypeRegistration.FUNCTION);
    addToBlacklist(RequirementTypeRegistration.CHECK_ENTITY);
    addToBlacklist(RequirementTypeRegistration.KILL_ENTITY);
    addToBlacklist(RequirementTypeRegistration.HEATH_ENTITY);
    addToBlacklist(RequirementTypeRegistration.SPAWN_ENTITY);
    addToBlacklist(RequirementTypeRegistration.COMMAND);
    addToBlacklist(RequirementTypeRegistration.EMPTY);
    addToBlacklist(RequirementTypeRegistration.HEIGHT);
    addToBlacklist(RequirementTypeRegistration.REDSTONE);
    addToBlacklist(RequirementTypeRegistration.STRUCTURE);
  }

  @SuppressWarnings("unchecked")
  public static <R extends IRequirement<C, T>, C extends MachineComponent<T>, T> void addToBlacklist(Supplier<RequirementType<R, C, T>> requirementType) {
    if (blacklist.contains(requirementType)) return;
    blacklist.add((Supplier<RequirementType<?, ?, ?>>) (Object) requirementType);
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
    if (blacklist.stream().anyMatch(c -> c.get().equals(requirementType)))
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
