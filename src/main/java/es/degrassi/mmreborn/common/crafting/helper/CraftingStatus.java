package es.degrassi.mmreborn.common.crafting.helper;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class CraftingStatus {
  public static final CraftingStatus SUCCESS = new CraftingStatus(Type.CRAFTING, null);
  public static final CraftingStatus MISSING_STRUCTURE = new CraftingStatus(Type.MISSING_STRUCTURE, null);
  public static final CraftingStatus NO_RECIPE = new CraftingStatus(Type.NO_RECIPE, null);

  public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatus> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public CraftingStatus decode(RegistryFriendlyByteBuf buffer) {
      Type type = buffer.readEnum(Type.class);
      Component message = null;
      if (buffer.readBoolean()) {
        message = Component.Serializer.fromJson(buffer.readUtf(), buffer.registryAccess());
      }
      return new CraftingStatus(type, message);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, CraftingStatus value) {
      buffer.writeEnum(value.status);
      buffer.writeBoolean(value.message != null);
      if (value.message != null)
        buffer.writeUtf(Component.Serializer.toJson(value.message, buffer.registryAccess()));
    }
  };

  @Getter
  private final Type status;
  @Nullable
  private final Component message;

  private CraftingStatus(Type status, @Nullable Component unlocMessage) {
    this.status = status;
    this.message = unlocMessage;
  }

  public Component getUnlocMessage() {
    return message != null ? message : this.status.getUnlocalizedDescription();
  }

  public boolean isCrafting() {
    return this.status.isCrafting();
  }

  public boolean isFailure() {
    return this.status.isFailure();
  }

  public boolean isMissingStructure() {
    return this.status == Type.MISSING_STRUCTURE;
  }

  public static CraftingStatus working() {
    return SUCCESS;
  }

  public static CraftingStatus failure(Component message) {
    return new CraftingStatus(Type.FAILURE, message);
  }

  public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    CompoundTag tag = new CompoundTag();
    tag.putString("type", this.status.getSerializedName());
    if (this.message != null)
      tag.putString("message", Component.Serializer.toJson(this.message, registries));
    return tag;
  }

  public static CraftingStatus deserialize(CompoundTag tag, HolderLookup.Provider registries) {
    if (tag.isEmpty()) return CraftingStatus.MISSING_STRUCTURE;
    Type type = Type.fromString(tag.getString("type"));
    Component message = null;
    if (tag.contains("message")) {
      message = Component.Serializer.fromJson(tag.getString("message"), registries);
    }
    return new CraftingStatus(type, message);
  }

  public static CraftingStatus of(Type type, Component message) {
    return new CraftingStatus(type, message);
  }

  public static CraftingStatus of(String type, Component message) {
    return new CraftingStatus(Type.fromString(type), message);
  }

  public enum Type implements StringRepresentable {
    MISSING_STRUCTURE,
    NO_RECIPE,
    FAILURE,
    CRAFTING;

    public static Type fromString(String value) {
      return switch (value.toLowerCase(Locale.ROOT)) {
        case "missing_structure" -> MISSING_STRUCTURE;
        case "crafting" -> CRAFTING;
        case "failure" -> FAILURE;
        default -> NO_RECIPE;
      };
    }

    public boolean isCrafting() {
      return this == CRAFTING;
    }

    public boolean isFailure() {
      return this == FAILURE;
    }

    public Component getUnlocalizedDescription() {
      return Component.translatable("gui.controller.status." + getSerializedName());
    }

    @Override
    public @NotNull String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }
}
