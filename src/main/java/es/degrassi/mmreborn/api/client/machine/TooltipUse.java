package es.degrassi.mmreborn.api.client.machine;

import es.degrassi.mmreborn.api.codec.NamedCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TooltipUse implements StringRepresentable {
  GUI,
  ITEM
  ;

  public static final NamedCodec<TooltipUse> CODEC = NamedCodec.enumCodec(TooltipUse.class);

  public boolean isGui() {
    return this == GUI;
  }

  public boolean isItem() {
    return this == ITEM;
  }

  @Override
  public @NotNull String getSerializedName() {
    return name().toLowerCase();
  }
}
