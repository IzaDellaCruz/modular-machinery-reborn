package es.degrassi.mmreborn.common.item;

import com.mojang.serialization.Codec;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum StructureCreatorItemMode implements StringRepresentable {
  SINGLE, BOX;

  public static final Codec<StructureCreatorItemMode> CODEC = NamedCodec.enumCodec(StructureCreatorItemMode.class).codec();
  public static final StreamCodec<RegistryFriendlyByteBuf, StructureCreatorItemMode> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public StructureCreatorItemMode decode(RegistryFriendlyByteBuf buffer) {
      return buffer.readEnum(StructureCreatorItemMode.class);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, StructureCreatorItemMode value) {
      buffer.writeEnum(value);
    }
  };

  public StructureCreatorItemMode next() {
    return isSingle() ? BOX : SINGLE;
  }

  public boolean isSingle() {
    return this == SINGLE;
  }

  public boolean isBox() {
    return this == BOX;
  }

  public MutableComponent component() {
    return Component.translatable("modular_machinery_reborn.structure_creator.mode." + getSerializedName());
  }

  @Override
  public String getSerializedName() {
    return name().toLowerCase(Locale.ROOT);
  }
}
