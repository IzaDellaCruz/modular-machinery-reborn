package es.degrassi.mmreborn.common.crafting.modifier;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.codec.RegistrarCodec;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;

@Getter
public abstract class RecipeModifier implements IRecipeModifier {

  public static final NamedCodec<RecipeModifier> CODEC = NamedCodec.record(energyModifierInstance ->
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
          case ADDITION -> new AdditionRecipeModifier(requirement, mode, modifier, chance, max, min);
          case MULTIPLICATION -> new MultiplicationRecipeModifier(requirement, mode, modifier, chance, max, min);
        };
      }), "Recipe modifier"
  );

  public static final List<RequirementType<?, ?>> blacklist = Lists.newArrayList();

  static {
    addToBlacklist(RequirementTypeRegistration.DIMENSION.get());
    addToBlacklist(RequirementTypeRegistration.BIOME.get());
    addToBlacklist(RequirementTypeRegistration.WEATHER.get());
    addToBlacklist(RequirementTypeRegistration.TIME.get());
    addToBlacklist(RequirementTypeRegistration.CHUNKLOAD.get());
    addToBlacklist(RequirementTypeRegistration.FUNCTION.get());
  }

  public static void addToBlacklist(RequirementType<?, ?> requirementType) {
    if (blacklist.contains(requirementType)) return;
    blacklist.add(requirementType);
  }

  public static final RandomSource RAND = RandomSource.create();

  public final RequirementType<?, ?> requirementType;
  public final IOType mode;
  public final float modifier;
  public final float chance;
  public final float max;
  public final float min;
  public final Component tooltip;

  protected RecipeModifier(RequirementType<?, ?> requirementType, IOType mode, float modifier, float chance, float max,
                   float min) {
    this.requirementType = requirementType;
    this.mode = mode;
    this.modifier = modifier;
    this.chance = chance;
    this.max = max;
    this.min = min;
    this.tooltip = getDefaultTooltip();
  }

  @Override
  public boolean shouldApply(RequirementType<?, ?> type, IOType mode) {
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
    return ModularMachineryReborn.getRequirementRegistrar().getKey(requirementType).getPath();
  }
}
