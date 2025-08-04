package es.degrassi.mmreborn.common.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class BlockCasing extends BlockMachineComponent {
  public BlockCasing() {
    super(
      Properties.of()
        .strength(2F, 10F)
        .sound(SoundType.METAL)
        .requiresCorrectToolForDrops()
        .dynamicShape()
        .noOcclusion()
    );
  }

  public enum CasingType implements StringRepresentable {
    PLAIN,
    VENT,
    FIREBOX,
    GEARBOX,
    REINFORCED,
    CIRCUITRY;

    @Override
    public @NotNull String getSerializedName() {
      return name().toLowerCase();
    }

    public static CasingType value(String name) {
      return switch(name.toLowerCase(Locale.ROOT)) {
        case "vent" -> VENT;
        case "firebox" -> FIREBOX;
        case "gearbox" -> GEARBOX;
        case "reinforced" -> REINFORCED;
        case "circuitry" -> CIRCUITRY;
        default -> PLAIN;
      };
    }
  }

}
