package es.degrassi.mmreborn.api;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class MinBlocksPredicate implements BiPredicate<BlockIngredient, BlockInWorld> {
  public static final NamedCodec<MinBlocksPredicate> CODEC = NamedCodec.unboundedMap(NamedCodec.STRING, MinMax.CODEC, "Map<String, MinMax>")
      .comapFlatMap(map -> DataResult.success(new MinBlocksPredicate(map)), MinBlocksPredicate::minBlocks, "MinBlocksPredicate");

  public static final MinBlocksPredicate EMPTY = new MinBlocksPredicate(Maps.newHashMap());

  private final Map<String, MinMax> minBlocks;
  private final Object2IntOpenHashMap<BlockIngredient> tests = new Object2IntOpenHashMap<>();

  public MinBlocksPredicate(Map<String, MinMax> minBlocks) {
    this.minBlocks = minBlocks;
  }

  public Map<String, MinMax> minBlocks() {
    return minBlocks;
  }

  public void reset() {
    tests.clear();
  }

  @Override
  public boolean test(BlockIngredient blockIngredient, BlockInWorld blockInWorld) {
    if (!blockIngredient.test(blockInWorld)) return false;
    MinMax toTest = minBlocks.get(blockIngredient.getId());
    if (toTest == null) return true;
    int newValue = tests.compute(blockIngredient, (b, current) -> Optional.ofNullable(current).orElse(0) + 1);
    return toTest.test(newValue);
  }

  public JsonElement asJson() {
    JsonObject json = new JsonObject();
    minBlocks.forEach((block, minmax) -> json.add(block, minmax.toJson()));
    return json;
  }

  public record MinMax(int min, int max) implements Predicate<Integer> {
    private static final int minValue = 0;
    private static final int maxValue = Integer.MAX_VALUE;
    public static final NamedCodec<MinMax> CODEC = NamedCodec.record(instance -> instance.group(
        NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("min", minValue).forGetter(MinMax::min),
        NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max", maxValue).forGetter(MinMax::max)
    ).apply(instance, MinMax::new), "MinMax");

    public static MinMax max(int max) {
      return new MinMax(0, max);
    }

    public static MinMax min(int min) {
      return new MinMax(min, Integer.MAX_VALUE);
    }

    public boolean test(Integer toTest) {
      return min <= toTest && toTest <= max;
    }

    public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("min", min);
      json.addProperty("max", max);
      return json;
    }

    public MutableComponent guiText() {
      if (min == minValue && max == maxValue) return Component.empty();
      if (min == minValue) return Component.translatable("mmr.controller.max", max);
      if (max == maxValue) return Component.translatable("mmr.controller.min", min);
      return Component.translatable("mmr.controller.min_max", min, max);
    }
  }
}
