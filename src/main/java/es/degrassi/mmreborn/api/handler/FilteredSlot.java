package es.degrassi.mmreborn.api.handler;

import es.degrassi.mmreborn.api.network.ISyncableStuff;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Predicate;

public interface FilteredSlot<VALUE> extends ISyncableStuff {
  int getSlot();

  void setFilter(Predicate<VALUE> filter);

  VALUE getValue();

  boolean isEmpty();


  boolean isInput();

  boolean isOutput();

  CompoundTag serializeNBT(HolderLookup.Provider pRegistries);

  void deserialize(HolderLookup.Provider pRegistries, CompoundTag componentNBT);
}
