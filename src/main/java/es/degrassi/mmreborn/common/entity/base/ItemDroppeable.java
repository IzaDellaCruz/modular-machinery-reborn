package es.degrassi.mmreborn.common.entity.base;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ItemDroppeable {
  void addDrops(List<ItemStack> drops);
}
