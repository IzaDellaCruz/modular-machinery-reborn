package es.degrassi.mmreborn.api.network.syncable;

import es.degrassi.mmreborn.api.network.AbstractSyncable;
import es.degrassi.mmreborn.api.network.data.ResourceLocationData;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ResourceLocationSyncable extends AbstractSyncable<ResourceLocationData, ResourceLocation> {
  @Override
  public ResourceLocationData getData(short id) {
    return new ResourceLocationData(id, get());
  }

  public static ResourceLocationSyncable create(Supplier<ResourceLocation> supplier,
                                                Consumer<ResourceLocation> consumer) {
    return new ResourceLocationSyncable() {
      @Override
      public ResourceLocation get() {
        return supplier.get();
      }

      @Override
      public void set(ResourceLocation value) {
        consumer.accept(value);
      }
    };
  }
}
