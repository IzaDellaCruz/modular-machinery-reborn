package es.degrassi.mmreborn.common.crafting.helper;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.codec.DefaultCodecs;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

@Getter
public class ProgressData extends DirectionalPositionedData {
  public static final ResourceLocation defaultEmpty = ModularMachineryReborn.rl("textures/gui/empty_arrow.png");
  public static final ResourceLocation defaultFilled = ModularMachineryReborn.rl("textures/gui/filled_arrow.png");

  public static final NamedCodec<ProgressData> CODEC = NamedCodec.record(instance -> instance.group(
        NamedCodec.enumCodec(Direction.class).optionalFieldOf("direction", Direction.LEFT).forGetter(ProgressData::direction),
        PositionedRequirement.POSITION_CODEC.optionalFieldOf("position", new PositionedRequirement(74, 8)).forGetter(ProgressData::position),
        DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("emptyTexture", defaultEmpty).forGetter(ProgressData::getEmptyTexture),
        DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("filledTexture", defaultFilled).forGetter(ProgressData::getFillTexture)
    ).apply(instance, ProgressData::new), "Directional Positioned Data");

  private final ResourceLocation emptyTexture, fillTexture;

  public ProgressData(Direction direction, PositionedRequirement position, ResourceLocation emptyTexture, ResourceLocation fillTexture) {
    super(direction, position);
    this.emptyTexture = emptyTexture;
    this.fillTexture = fillTexture;
  }

  public static final ProgressData DEFAULT_PROGRESS = new ProgressData(
      Direction.LEFT,
      new PositionedRequirement(74, 8),
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
