package es.degrassi.mmreborn.common.util;

import net.neoforged.fml.ModList;

public interface Mods {
  static boolean isAULoaded() {
    return ModList.get().isLoaded("almostunified");
  }

  static boolean isAthenaLoaded() {
    return ModList.get().isLoaded("athena");
  }

  static boolean isJEILoaded() {
    return ModList.get().isLoaded("jei");
  }
  static boolean isEMILoaded() {
    return ModList.get().isLoaded("emi");
  }

  static boolean isJEIorEMILoaded() {
    return isEMILoaded() || isJEILoaded();
  }
}
