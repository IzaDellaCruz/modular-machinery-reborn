package es.degrassi.mmreborn.api.capability;

import es.degrassi.experiencelib.api.capability.IContentsListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IFuelHandler {
  String FUEL_TAG = "fuel";
  String MAX_FUEL_TAG = "maxFuel";

  long getFuel();
  long getMaxFuel();
  void addFuel(long fuel);

  void setFuel(long fuel);
  void setMaxFuel(long maxFuel);

  boolean burn(long amount);
  void tryBurnItem();

  default void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
    nbt.putLong(FUEL_TAG, getFuel());
    nbt.putLong(MAX_FUEL_TAG, getMaxFuel());
  }

  default void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
    if (nbt.contains(FUEL_TAG, CompoundTag.TAG_LONG))
      setFuel(nbt.getLong(FUEL_TAG));
    if (nbt.contains(MAX_FUEL_TAG, CompoundTag.TAG_LONG))
      setMaxFuel(nbt.getLong(MAX_FUEL_TAG));
  }

  void setChanged();
  void setListener(IContentsListener listener);
}
