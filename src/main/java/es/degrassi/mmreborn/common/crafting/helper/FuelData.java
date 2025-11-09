package es.degrassi.mmreborn.common.crafting.helper;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.codec.DefaultCodecs;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

@Getter
public class FuelData extends DirectionalPositionedData {
  public static final ResourceLocation defaultEmpty = ModularMachineryReborn.rl("textures/gui/empty_fuel.png");
  public static final ResourceLocation defaultFilled = ModularMachineryReborn.rl("textures/gui/filled_fuel.png");

  public static final NamedCodec<FuelData> CODEC = NamedCodec.record(instance -> instance.group(
        NamedCodec.enumCodec(Direction.class).optionalFieldOf("direction", Direction.BOTTOM).forGetter(FuelData::direction),
        PositionedRequirement.POSITION_CODEC.optionalFieldOf("position", new PositionedRequirement(0, 0)).forGetter(FuelData::position),
        DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("emptyTexture", defaultEmpty).forGetter(FuelData::getEmptyTexture),
        DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("filledTexture", defaultFilled).forGetter(FuelData::getFillTexture)
    ).apply(instance, FuelData::new), "Directional Positioned Data");

  private final ResourceLocation emptyTexture, fillTexture;

  public FuelData(Direction direction, PositionedRequirement position, ResourceLocation emptyTexture, ResourceLocation fillTexture) {
    super(direction, position);
    this.emptyTexture = emptyTexture;
    this.fillTexture = fillTexture;
  }

  public static final FuelData DEFAULT_FUEL = new FuelData(
      Direction.BOTTOM,
      new PositionedRequirement(0, 0),
      defaultEmpty,
      defaultFilled
  );

  @Override
  public JsonObject asJson() {
    JsonObject json = super.asJson();
    json.addProperty("emptyTexture", emptyTexture.toString());
    json.addProperty("filledTexture", fillTexture.toString());
    return json;
  }
}
