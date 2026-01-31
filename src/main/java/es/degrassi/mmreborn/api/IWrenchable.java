package es.degrassi.mmreborn.api;

import es.degrassi.mmreborn.api.capability.config.RelativeSide;
import net.minecraft.world.entity.player.Player;

public interface IWrenchable {
  /**
   * Called when clicked on a block face
   * @param side side clicked
   */
  Result onWrenched(RelativeSide side, Player player);

  enum Result{
    SUCCESS,
    FAIL,
    NONE
  }
}
