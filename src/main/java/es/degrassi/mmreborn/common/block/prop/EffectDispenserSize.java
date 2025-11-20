package es.degrassi.mmreborn.common.block.prop;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum EffectDispenserSize implements ConfigLoaded, StringRepresentable {
  SMALL(5, false, true),
  MEDIUM(15, false, true),
  BIG(32, false, true);

  public final int defaultRadius;
  public final boolean defaultDimensional;
  public final boolean defaultShowParticles;
  public int radius;
  public boolean interdimensional, showParticles;

  EffectDispenserSize(int radius, boolean interdimensional, boolean showParticles) {
    this.defaultRadius = radius;
    this.defaultDimensional = interdimensional;
    this.defaultShowParticles = showParticles;
  }

  public static EffectDispenserSize value(String value) {
    return switch(value.toUpperCase(Locale.ENGLISH)) {
      case "MEDIUM" -> MEDIUM;
      case "BIG" -> BIG;
      default -> SMALL;
    };
  }

  @Override
  public String getSerializedName() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
