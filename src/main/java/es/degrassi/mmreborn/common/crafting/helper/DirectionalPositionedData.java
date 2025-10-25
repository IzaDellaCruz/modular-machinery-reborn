package es.degrassi.mmreborn.common.crafting.helper;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;

public class DirectionalPositionedData {
  public static NamedCodec<DirectionalPositionedData> createCodec(Direction defaultDirection, PositionedRequirement defaultPosition) {
    return NamedCodec.record(instance -> instance.group(
        NamedCodec.enumCodec(Direction.class).optionalFieldOf("direction", defaultDirection).forGetter(DirectionalPositionedData::direction),
        PositionedRequirement.POSITION_CODEC.optionalFieldOf("position", defaultPosition).forGetter(DirectionalPositionedData::position)
    ).apply(instance, DirectionalPositionedData::new), "Directional Positioned Data");
  }

  private final Direction direction;
  private final PositionedRequirement position;

  public DirectionalPositionedData(Direction direction, PositionedRequirement position) {
    this.direction = direction;
    this.position = position;
  }

  public Direction direction() {
    return this.direction;
  }

  public PositionedRequirement position() {
    return this.position;
  }

  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    json.addProperty("direction", direction.toString());
    json.add("position", position.asJson());
    return json;
  }
}
