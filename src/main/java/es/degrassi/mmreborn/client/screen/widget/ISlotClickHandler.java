package es.degrassi.mmreborn.client.screen.widget;

import es.degrassi.mmreborn.client.container.SlotItemComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ISlotClickHandler {

  void onClick(Supplier<@Nullable SlotItemComponent> slotProvider, int button, boolean hasShiftDown, ItemStack heldItem);
}
