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

  static boolean isLDLibLoaded() {
    return ModList.get().isLoaded("ldlib2");
  }

  static boolean shouldAddExpHandlerToAe2() {
    return ModList.get().isLoaded("ae2") && ModList.get().isLoaded("appex");
  }
}
