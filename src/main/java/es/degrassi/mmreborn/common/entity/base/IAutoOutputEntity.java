package es.degrassi.mmreborn.common.entity.base;

public interface IAutoOutputEntity {
  /* Handle Auto Output Stuff */
  boolean isShouldAutoOutput();

  void setShouldAutoOutput(boolean shouldAutoOutput);

  void tickAutoOutput();
}
