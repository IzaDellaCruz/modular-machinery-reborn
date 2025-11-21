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
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import es.degrassi.mmreborn.common.util.HybridTank;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

@Getter
public class RequirementFluid implements IRequirement<FluidComponent, HybridTank> {
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
  public RequirementType<RequirementFluid, HybridTank> getType() {
    return RequirementTypeRegistration.FLUID.get();
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_FLUID.get();
  }

  @Override
  public boolean test(FluidComponent component, ICraftingContext context) {
    HybridTank handler = component.getContainerProvider();
    return switch (getMode()) {
      case INPUT -> {
        int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
        yield this.ingredient.test(handler.getFluid()) && amount <= handler.getFluidAmount();
      }
      case OUTPUT -> {
        int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
        yield (handler.isEmpty() || this.ingredient.test(handler.getFluid())) && amount <= handler.getSpace();
      }
      case NONE -> true;
    };
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
    int maxExtract = component.getContainerProvider().getFluidAmount();

    if (maxExtract >= amount) {
      component.removeFromInputs(this.ingredient.ingredient(), amount);
      return CraftingResult.success();
    }

    return errorInput(amount, component.getContainerProvider().getFluid(), component.getContainerProvider().getFluidAmount());
  }

  private CraftingResult errorInput(int amount, FluidStack found, int amountFound) {
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.fluid.input",
        amount, ingredient.toString(),
        amountFound, found.getHoverName()
    ));
  }

  private CraftingResult errorOutput(FluidStack found) {
    return CraftingResult.error(Component.translatable(
        "craftcheck.failure.fluid.output.fluid",
        ingredient.toString(),
        found.getHoverName()
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
    HybridTank handler = component.getContainerProvider();
    var output = ingredient.getFluids()[0];
    if (!handler.isEmpty() && !ingredient.test(handler.getFluid()))
      return errorOutput(handler.getFluid());
    int amount = (int) context.getIntegerModifiedValue(this.ingredient.amount(), this);
    int canFill = handler.getSpace();
    if (canFill >= amount) {
      component.addToOutputs(output.copyWithAmount(amount));
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
  public @NotNull Component getMissingComponentErrorMessage(IOType ioType) {
    return Component.translatable(String.format("component.missing.fluid.%s", ioType.name().toLowerCase()));
  }

  @Override
  public boolean isComponentValid(FluidComponent m, ICraftingContext context) {
    if (getMode().isInput()) {
      if (m.getContainerProvider().isEmpty()) return false;
    } else {
      if (m.getContainerProvider().isEmpty()) return true;
    }
    return ingredient.test(m.getContainerProvider().getFluid());
  }
}
