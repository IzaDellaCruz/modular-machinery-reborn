package es.degrassi.mmreborn.common.machine;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.Locale;

public enum IOType implements StringRepresentable {
  INPUT,
  OUTPUT,
  NONE;

  public static final NamedCodec<IOType> CODEC = NamedCodec.enumCodec(IOType.class);

  public static IOType value(String mode) {
    return valueOf(mode.toUpperCase(Locale.ENGLISH));
  }

  @Nullable
  public static IOType getByString(String name) {
    for (IOType val : values()) {
      if (val.name().equalsIgnoreCase(name)) {
        return val;
      }
    }
    return null;
  }

  public boolean isInput() {
    return this == INPUT;
  }

  public boolean isOutput() {
    return this == OUTPUT;
  }

  @Override
  public String getSerializedName() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String getTranslationKey() {
    return ModularMachineryReborn.MODID + ".requirement.mode." + getSerializedName();
  }
}
