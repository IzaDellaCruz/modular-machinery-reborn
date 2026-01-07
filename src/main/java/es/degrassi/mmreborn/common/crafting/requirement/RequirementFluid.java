package es.degrassi.mmreborn.common.crafting.requirement;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.codec.NamedMapCodec;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirementList;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.manager.handler.FluidHandler;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@Getter
public class RequirementFluid implements IRequirement<FluidComponent, FluidHandler> {
  public static final NamedMapCodec<RequirementFluid> CODEC = NamedCodec.record(instance -> instance.group(
      NamedCodec.of(SizedFluidIngredient.FLAT_CODEC).fieldOf("fluid").forGetter(req -> req.ingredient),
      NamedCodec.enumCodec(IOType.class).fieldOf("mode").forGetter(IRequirement::getMode),
      PositionedRequirement.POSITION_CODEC.optionalFieldOf("position", new PositionedRequirement(0, 0)).forGetter(IRequirement::getPosition)
  ).apply(instance, (fluid, mode, position) -> new RequirementFluid(mode, fluid, position)),
      "FluidRequirement");

  private final PositionedRequirement position;
  private final IOType mode;
  private final SizedFluidIngredient ingredient;

  public RequirementFluid(IOType ioType, SizedFluidIngredient fluid, PositionedRequirement position) {
    this.ingredient = fluid;
    this.position = position;
    this.mode = ioType;
  }

  @Override
  public RequirementType<RequirementFluid, FluidComponent, FluidHandler> getType() {
    return RequirementTypeRegistration.FLUID.get();
  }

  @Override
  public ComponentType<FluidHandler> getComponentType() {
    return ComponentRegistration.COMPONENT_FLUID.get();
  }

  @Override
  public boolean test(FluidComponent component, ICraftingContext context) {
    FluidHandler handler = component.getContainerProvider();
    return switch (getMode()) {
      case INPUT -> {
        int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
        yield handler.getIngredientAmount(this.ingredient.ingredient()) >= amount;
      }
      case OUTPUT -> {
        int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
        yield handler.getSpaceForFluid(this.output()) >= amount;
      }
      case NONE -> true;
    };
  }

  private FluidStack output() {
    return this.ingredient.getFluids()[0];
  }

  @Override
  public void gatherRequirements(IRequirementList<FluidComponent> list) {
    switch (getMode()) {
      case INPUT -> list.processOnStart(this::processInput);
      case OUTPUT -> list.processOnEnd(this::processOutput);
    }
  }

  private CraftingResult processInput(FluidComponent component, ICraftingContext context) {
    int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
    int maxExtract = component.getContainerProvider().getFluidAmount(ingredient.ingredient());

    if (maxExtract >= amount) {
      component.removeFromInputs(this.ingredient.ingredient(), amount);
      return CraftingResult.success();
    }

    return errorInput(amount, component.getContainerProvider().getFluids(),
        component.getContainerProvider().getFluidAmount(ingredient.ingredient()));
  }

  private CraftingResult errorInput(int amount, FluidIngredient found, int amountFound) {
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.fluid.input",
        amount, ingredient.toString(),
        amountFound, found.toString()
    ));
  }

  private CraftingResult errorOutput(FluidIngredient found) {
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.fluid.output.fluid",
        ingredient.toString(),
        found.toString()
    ));
  }

  private CraftingResult errorOutput(int amount, int requiredSpace) {
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.fluid.output.space",
        requiredSpace,
        amount
    ));
  }

  private CraftingResult processOutput(FluidComponent component, ICraftingContext context) {
    FluidHandler handler = component.getContainerProvider();
    if (!handler.isEmpty() && !handler.contains(ingredient.ingredient()))
      return errorOutput(handler.getFluids());
    int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
    int canFill = handler.getSpaceForFluid(output());
    if (canFill >= amount) {
      component.addToOutputs(output().copyWithAmount(amount));
      return CraftingResult.success();
    }
    return errorOutput(canFill, amount);
  }

  @Override
  public JsonObject asJson() {
    JsonObject json = IRequirement.super.asJson();
    json.addProperty("fluid", ingredient.toString());
    json.addProperty("amount", ingredient.amount());
    return json;
  }

  @Override
  public Component getMissingComponentErrorMessage(IOType ioType) {
    return Component.translatable(String.format("component.missing.fluid.%s", ioType.name().toLowerCase()));
  }

  @Override
  public boolean isComponentValid(FluidComponent m, ICraftingContext context) {
    if (getMode().isInput()) {
      if (m.getContainerProvider().isEmpty()) return false;
    } else {
      if (m.getContainerProvider().isEmpty()) return true;
    }
    return m.getContainerProvider().contains(ingredient.ingredient());
  }
}
