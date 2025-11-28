package es.degrassi.mmreborn.common.block.prop;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum FuelTankSize implements ConfigLoaded, StringRepresentable {
  TINY(secondsToTicks(500)),
  SMALL(secondsToTicks(1000)),
  NORMAL(secondsToTicks(2500)),
  REINFORCED(secondsToTicks(5000)),
  BIG(secondsToTicks(7500)),
  HUGE(secondsToTicks(10000));

  public long burnTimeCapacity;
  public int stackSize;

  public final long defaultBurnTimeCapacity;
  public final int defaultStackSize;

  FuelTankSize(long defaultBurnTimeCapacity) {
    this.defaultBurnTimeCapacity = defaultBurnTimeCapacity;
    this.defaultStackSize = 64;
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
