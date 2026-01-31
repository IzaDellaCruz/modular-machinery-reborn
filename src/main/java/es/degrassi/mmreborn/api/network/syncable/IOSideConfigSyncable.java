package es.degrassi.mmreborn.api.network.syncable;

import es.degrassi.mmreborn.api.capability.config.IOSideConfig;
import es.degrassi.mmreborn.api.network.AbstractSyncable;
import es.degrassi.mmreborn.api.network.data.IOSideConfigData;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class IOSideConfigSyncable extends AbstractSyncable<IOSideConfigData, IOSideConfig> {

  @Override
  public IOSideConfigData getData(short id) {
    return new IOSideConfigData(id, get());
  }

  @Override
  public boolean needSync() {
    IOSideConfig value = get();
    if(this.lastKnownValue == null) {
      this.lastKnownValue = value.copy();
      return true;
    }
    if(!this.lastKnownValue.equals(value)) {
      this.lastKnownValue = value.copy();
      return true;
    }
    return false;
  }

  public static IOSideConfigSyncable create(Supplier<IOSideConfig> supplier, Consumer<IOSideConfig> consumer) {
    return new IOSideConfigSyncable() {
      @Override
      public IOSideConfig get() {
        return supplier.get();
      }

      @Override
      public void set(IOSideConfig value) {
        consumer.accept(value);
      }
    };
  }
}
