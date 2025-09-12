package es.degrassi.mmreborn.common.crafting.requirement;

import com.google.gson.JsonObject;
import es.degrassi.experiencelib.api.capability.IExperienceHandler;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.codec.NamedMapCodec;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirementList;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.ExperienceComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public class RequirementExperiencePerTick implements IRequirement<ExperienceComponent> {
  public static final NamedMapCodec<RequirementExperiencePerTick> CODEC = NamedCodec.record(instance -> instance.group(
      NamedCodec.longRange(0, Long.MAX_VALUE).fieldOf("amount").forGetter(req -> req.required),
      NamedCodec.enumCodec(IOType.class).fieldOf("mode").forGetter(IRequirement::getMode),
      PositionedRequirement.POSITION_CODEC.optionalFieldOf("position", new PositionedRequirement(0, 0)).forGetter(IRequirement::getPosition)
  ).apply(instance, (amount, type, position) -> new RequirementExperiencePerTick(type, amount, position)), "EnergyRequirement");

  private final IOType mode;
  private final PositionedRequirement position;
  public final long required;

  public RequirementExperiencePerTick(IOType actionType, long amount, PositionedRequirement position) {
    this.required = amount;
    this.position = position;
    this.mode = actionType;
  }

  @Override
  public RequirementType<RequirementExperiencePerTick> getType() {
    return RequirementTypeRegistration.EXPERIENCE_PER_TICK.get();
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_EXPERIENCE.get();
  }

  @Override
  public boolean test(ExperienceComponent component, ICraftingContext context) {
    IExperienceHandler handler = component.getContainerProvider();
    return switch (mode) {
      case INPUT -> handler.getExperience() >= required;
      case OUTPUT -> handler.getExperienceCapacity() >= handler.getExperience() + required;
      case NONE -> true;
    };
  }

  @Override
  public void gatherRequirements(IRequirementList<ExperienceComponent> list) {
    if (mode.isInput()) {
      list.processEachTick(this::processInput);
    } else if (mode.isOutput()) {
      list.processEachTick(this::processOutput);
    }
  }

  private CraftingResult processInput(ExperienceComponent component, ICraftingContext context) {
    long amount = (long) context.getModifiedValue(required, this);
    final long originAmount = amount;
    long canExtract = 0;
    for (int i = 0; i < component.getContainerProvider().getTanks(); i++) {
       long toExtract = component.getContainerProvider().extractExperienceRecipe(i, amount, true);
       canExtract += toExtract;
       amount -= toExtract;
    }
    if (canExtract == originAmount) {
      for (int i = 0; i < component.getContainerProvider().getTanks(); i++) {
        long toExtract = component.getContainerProvider().extractExperienceRecipe(i, canExtract, false);
        canExtract += toExtract;
      }
      return CraftingResult.success();
    }
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.experience.input", required, component.getContainerProvider().getExperience()
    ));
  }

  private CraftingResult processOutput(ExperienceComponent component, ICraftingContext context) {
    IExperienceHandler handler = component.getContainerProvider();
    long amount = (long) context.getModifiedValue(required, this);
    long remaining = handler.getExperienceCapacity() - handler.getExperience();
    if (remaining >= amount) {
      for (int i = 0; i < component.getContainerProvider().getTanks(); i++) {
        amount -= component.getContainerProvider().receiveExperienceRecipe(i, amount, false);
      }
      return CraftingResult.success();
    }
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.experience.output", required,
        component.getContainerProvider().getExperienceCapacity() - component.getContainerProvider().getExperience()
    ));
  }

  @Override
  public JsonObject asJson() {
    JsonObject json = IRequirement.super.asJson();
    json.addProperty("amount", required);
    return json;
  }

  @Override
  public @NotNull Component getMissingComponentErrorMessage(IOType ioType) {
    return Component.translatable(String.format("component.missing.experience.%s", ioType.name().toLowerCase()));
  }

  @Override
  public boolean isComponentValid(ExperienceComponent m, ICraftingContext context) {
    return getMode().equals(m.getIOType());
  }
}
