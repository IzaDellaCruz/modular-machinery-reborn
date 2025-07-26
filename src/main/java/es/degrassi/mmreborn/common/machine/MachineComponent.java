package es.degrassi.mmreborn.common.machine;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MachineComponent<T> implements Comparable<MachineComponent<T>> {
  private final IOType ioType;

  protected MachineComponent(IOType ioType) {
    this.ioType = ioType;
  }

  public final IOType getIOType() {
    return ioType;
  }

  public abstract ComponentType getComponentType();

  @Nullable
  public abstract T getContainerProvider();

  public void onStatusChanged(MachineStatus oldStatus, MachineStatus newStatus, Component errorMessage) {
  }

  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", getComponentType().getId().toString());
    json.addProperty("mode", getIOType().getSerializedName());
    return json;
  }

  public CompoundTag asTag(HolderLookup.Provider provider) {
    CompoundTag tag = new CompoundTag();
    tag.putString("type", getComponentType().getId().toString());
    tag.putString("mode", getIOType().getSerializedName());
    return tag;
  }

  @Override
  public String toString() {
    return asJson().toString();
  }

  public abstract <C extends MachineComponent<?>> C merge(C c);

  public <C extends MachineComponent<?>> boolean canMerge(C c) {
    return c.getIOType().equals(getIOType());
  }

  @Override
  public int compareTo(@NotNull MachineComponent<T> o) {
    return 0;
  }
}
