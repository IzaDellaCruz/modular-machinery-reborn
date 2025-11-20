package es.degrassi.mmreborn.common.entity.base;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

public interface IServerTickEntity {
  @OverridingMethodsMustInvokeSuper
  default void doRestrictedTick() {
    if (this instanceof IAutoEntity<?>) {
      if (this instanceof IAutoInputEntity auto)
        auto.tickAutoInput();
      if (this instanceof IAutoOutputEntity auto)
        auto.tickAutoOutput();
    }
  }
}
