package es.degrassi.mmreborn.common.integration.kubejs.builder;

import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.common.crafting.helper.Direction;
import es.degrassi.mmreborn.common.crafting.helper.FuelData;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FuelDataJS {
  private Optional<Integer> x = Optional.empty(), y = Optional.empty();
  private Optional<ResourceLocation> empty = Optional.empty(), filled = Optional.empty();
  private Optional<Direction> direction = Optional.empty();
  private FuelDataJS() {}

  public static FuelDataJS create() {
    return new FuelDataJS();
  }

  public static FuelDataJS of(@Nullable Integer x, @Nullable Integer y, @Nullable Direction direction,
                              @Nullable ResourceLocation emptyTexture, @Nullable ResourceLocation filledTexture) {
    return new FuelDataJS()
        .x(x)
        .y(y)
        .direction(direction)
        .emptyTexture(emptyTexture)
        .filledTexture(filledTexture);
  }

  public static FuelDataJS of(@Nullable Integer x, @Nullable Integer y) {
    return new FuelDataJS()
        .x(x)
        .y(y);
  }

  public static FuelDataJS of(@Nullable ResourceLocation emptyTexture, @Nullable ResourceLocation filledTexture) {
    return new FuelDataJS()
        .emptyTexture(emptyTexture)
        .filledTexture(filledTexture);
  }

  public static FuelDataJS of(@Nullable Direction direction,
                              @Nullable ResourceLocation emptyTexture, @Nullable ResourceLocation filledTexture) {
    return new FuelDataJS()
        .direction(direction)
        .emptyTexture(emptyTexture)
        .filledTexture(filledTexture);
  }

  public static FuelDataJS of(@Nullable Integer x, @Nullable Integer y,
                              @Nullable ResourceLocation emptyTexture, @Nullable ResourceLocation filledTexture) {
    return new FuelDataJS()
        .x(x)
        .y(y)
        .emptyTexture(emptyTexture)
        .filledTexture(filledTexture);
  }

  public FuelDataJS x(@Nullable Integer x) {
    this.x = Optional.ofNullable(x);
    return this;
  }

  public FuelDataJS y(@Nullable Integer y) {
    this.y = Optional.ofNullable(y);
    return this;
  }

  public FuelDataJS emptyTexture(@Nullable ResourceLocation texture) {
    this.empty = Optional.ofNullable(texture);
    return this;
  }

  public FuelDataJS filledTexture(@Nullable ResourceLocation texture) {
    this.filled = Optional.ofNullable(texture);
    return this;
  }

  public FuelDataJS direction(@Nullable Direction direction) {
    this.direction = Optional.ofNullable(direction);
    return this;
  }

  @HideFromJS
  public FuelData build() {
    return new FuelData(
        direction.orElse(Direction.BOTTOM),
        new PositionedRequirement(
            x.orElse(0),
            y.orElse(0)
        ),
        empty.orElse(FuelData.defaultEmpty),
        filled.orElse(FuelData.defaultFilled)
    );
  }
}
