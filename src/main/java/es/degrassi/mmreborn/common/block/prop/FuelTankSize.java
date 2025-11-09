package es.degrassi.mmreborn.common.block.prop;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum FuelTankSize implements ConfigLoaded, StringRepresentable {
  TINY(secondsToTicks(5)),
  SMALL(secondsToTicks(10)),
  NORMAL(secondsToTicks(25)),
  REINFORCED(secondsToTicks(50)),
  BIG(secondsToTicks(75)),
  HUGE(secondsToTicks(100));

  public long burnTimeCapacity;
  public final long defaultBurnTimeCapacity;

  FuelTankSize(long defaultBurnTimeCapacity) {
    this.defaultBurnTimeCapacity = defaultBurnTimeCapacity;
  }

  public static FuelTankSize value(String value) {
    return switch(value.toUpperCase(Locale.ROOT)) {
      case "SMALL" -> SMALL;
      case "NORMAL" -> NORMAL;
      case "REINFORCED" -> REINFORCED;
      case "BIG" -> BIG;
      case "HUGE" -> HUGE;
      default -> TINY;
    };
  }

  private static long secondsToTicks(long seconds) {
    return seconds * 20;
  }

  @Override
  public String getSerializedName() {
    return name().toLowerCase(Locale.ROOT);
  }
}
