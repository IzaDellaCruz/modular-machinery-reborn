package es.degrassi.mmreborn.common.crafting.requirement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.degrassi.mmreborn.api.codec.DefaultCodecs;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirementList;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.DimensionComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RequirementDimension implements IRequirement<DimensionComponent> {
  public static final NamedCodec<RequirementDimension> CODEC = NamedCodec.record(instance -> instance.group(
      DefaultCodecs.RESOURCE_LOCATION.listOf().fieldOf("filter").forGetter(RequirementDimension::filter),
      NamedCodec.BOOL.optionalFieldOf("blacklist", false).forGetter(RequirementDimension::blacklist),
      PositionedRequirement.POSITION_CODEC.optionalFieldOf("position", new PositionedRequirement(0, 0)).forGetter(IRequirement::getPosition)
  ).apply(instance, RequirementDimension::new), "Dimension Requirement");

  @Getter
  private final PositionedRequirement position;
  private final List<ResourceLocation> filter;
  private final boolean blacklist;

  public RequirementDimension(List<ResourceLocation> filter, boolean blacklist, PositionedRequirement position) {
    this.filter = filter;
    this.blacklist = blacklist;
    this.position = position;
  }

  public List<ResourceLocation> filter() {
    return filter;
  }

  public boolean blacklist() {
    return blacklist;
  }

  @Override
  public RequirementType<RequirementDimension> getType() {
    return RequirementTypeRegistration.DIMENSION.get();
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_DIMENSION.get();
  }

  @Override
  public IOType getMode() {
    return IOType.INPUT;
  }

  @Override
  public boolean test(DimensionComponent component, ICraftingContext context) {
    return filter.contains(component.getContainerProvider()) != blacklist;
  }

  @Override
  public void gatherRequirements(IRequirementList<DimensionComponent> list) {
    // If the {@link #test(DimensionComponent, ICraftingContext)} success then this requirement is OK to proceed
  }

  @Override
  public JsonObject asJson() {
    JsonObject json = IRequirement.super.asJson();
    json.addProperty("blacklist", blacklist);
    JsonArray array = new JsonArray();
    filter.stream().map(ResourceLocation::toString).forEach(array::add);
    json.add("filter", array);
    return json;
  }

  @Override
  public @NotNull Component getMissingComponentErrorMessage(IOType ioType) {
    return Component.translatable("component.missing.dimension");
  }

  @Override
  public boolean isComponentValid(DimensionComponent m, ICraftingContext context) {
    return getMode().equals(m.getIOType());
  }
}
