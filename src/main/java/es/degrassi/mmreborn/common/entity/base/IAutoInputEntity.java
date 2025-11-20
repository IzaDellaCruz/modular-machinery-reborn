package es.degrassi.mmreborn.common.entity.base;

public interface IAutoInputEntity {
  /* Handle Auto Output Stuff */
  boolean isShouldAutoInput();

  void setShouldAutoInput(boolean shouldAutoInput);

  void tickAutoInput();
}
