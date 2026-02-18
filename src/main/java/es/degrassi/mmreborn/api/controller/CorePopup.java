package es.degrassi.mmreborn.api.controller;

import com.mojang.serialization.Codec;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record CorePopup(int maxCores, Map<String, List<CompoundTag>> pages) {
  private static final Codec<Map<String, List<CompoundTag>>> PAGES = NamedCodec.unboundedMap(
      NamedCodec.STRING,
      NamedCodec.of(CompoundTag.CODEC).listOf(),
      "CorePages"
  ).codec();

  public static final StreamCodec<RegistryFriendlyByteBuf, CorePopup> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public @NotNull CorePopup decode(RegistryFriendlyByteBuf buffer) {
      int maxCores = buffer.readInt();
      Map<String, List<CompoundTag>> pages = buffer.readJsonWithCodec(PAGES);
      return new CorePopup(maxCores, pages);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, CorePopup data) {
      buffer.writeInt(data.maxCores);
      buffer.writeJsonWithCodec(PAGES, data.pages);
    }
  };
}
