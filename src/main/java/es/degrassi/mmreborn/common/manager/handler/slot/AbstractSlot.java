package es.degrassi.mmreborn.common.manager.handler.slot;

import es.degrassi.mmreborn.api.handler.FilteredSlot;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

@Getter
@Setter
public abstract class AbstractSlot<VALUE, UNIT> implements FilteredSlot<VALUE> {
  protected final int slot;
  protected final UNIT capacity;
  protected final UNIT maxInput;
  protected final UNIT maxOutput;
  protected final Predicate<UNIT> maxIOTest;
  protected Predicate<VALUE> filter;
  protected VALUE value;
  protected boolean bypassLimit = false;

  protected AbstractSlot(int slot, VALUE defaultValue, UNIT capacity, UNIT maxInput, UNIT maxOutput, Predicate<VALUE> defaultFilter, Predicate<UNIT> maxIOTest) {
    this.slot = slot;
    this.value = defaultValue;
    this.capacity = capacity;
    this.maxInput = maxInput;
    this.maxOutput = maxOutput;
    this.filter = defaultFilter;
    this.maxIOTest = maxIOTest;
  }

  public boolean isInput() {
    return maxIOTest.test(maxInput);
  }

  public boolean isOutput() {
    return maxIOTest.test(maxOutput);
  }
}
