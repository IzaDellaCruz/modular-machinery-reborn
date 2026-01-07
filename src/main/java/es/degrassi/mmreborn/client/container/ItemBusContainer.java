package es.degrassi.mmreborn.client.container;

import es.degrassi.mmreborn.client.ModularMachineryRebornClient;
import es.degrassi.mmreborn.client.screen.widget.ISlotClickHandler;
import es.degrassi.mmreborn.common.block.prop.ItemBusSize;
import es.degrassi.mmreborn.common.data.config.ItemBusConfig;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import es.degrassi.mmreborn.common.registration.ContainerRegistration;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Setter
public class ItemBusContainer extends ContainerBase<TileItemBus> implements ISlotClickHandler {
  public static final int MAX_VISIBLE_ROWS = 5;
  public static final int SLOT_SIZE = 18;

  public static void open(ServerPlayer player, TileItemBus machine) {
    player.openMenu(new MenuProvider() {
      @Override
      public @NotNull Component getDisplayName() {
        return Component.translatable("modular_machinery_reborn.gui.title.item_bus");
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ItemBusContainer(id, inv, machine);
      }
    }, buf -> buf.writeBlockPos(machine.getBlockPos()));
  }

  public ItemBusContainer(int id, Inventory playerInv, TileItemBus entity) {
    super(entity, playerInv.player, ContainerRegistration.ITEM_BUS.get(), id);
  }

  public ItemBusContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
    this(id, inv, ModularMachineryRebornClient.getClientSideItemBusEntity(buffer.readBlockPos()));
  }

  @Override
  public void init() {
    super.init();
    addInventorySlots(getEntity().getInventory(), getEntity().getSize(), new AtomicInteger(this.getFirstComponentSlotIndex()));
  }

  protected void addPlayerSlots(AtomicInteger slotIndex) {
    int totalRows = (int) Math.ceil(getEntity().getSlots() * 1.0 / getEntity().getSize().cols);
    boolean needsScrolling = totalRows > MAX_VISIBLE_ROWS;

    int visibleRows = needsScrolling ? MAX_VISIBLE_ROWS : totalRows;
    int yOffset = visibleRows * SLOT_SIZE + 18;

    int lastYOffset;
    for (int i = 0; i < 9; i++) {
      addSyncedSlot(new Slot(player.getInventory(), slotIndex.getAndIncrement(), 8 + i * 18, yOffset + 18 * 3 + 3));
    }
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        lastYOffset = yOffset + i * 18;
        addSyncedSlot(new Slot(player.getInventory(), slotIndex.getAndIncrement(), 8 + j * 18, lastYOffset));
      }
    }
  }

  protected void addInventorySlots(ItemHandler itemHandler, ItemBusSize size, AtomicInteger atomicInteger) {
    int xOffset = ItemBusConfig.get().itemSlotXOffset.get();
    int yOffset = ItemBusConfig.get().itemSlotYOffset.get();
    int cols = size.cols;
    int row = 0;
    for (int s = 0, c = 0; s < size.slots; s++, c++) {
      if (c >= cols) {
        c = 0;
        row++;
      }
      addSyncedSlot(new SlotItemComponent(itemHandler.getInventory().get(s), atomicInteger.getAndIncrement(), xOffset + c * 18, yOffset + row * 18));
    }
  }

  public List<SlotItemComponent> getItemList() {
    return slots.stream().filter(slot -> slot.getSlotIndex() >= getFirstComponentSlotIndex() && slot instanceof SlotItemComponent)
        .map(slot -> (SlotItemComponent)slot).toList();
  }

  @Override
  public void onClick(Supplier<@Nullable SlotItemComponent> slotProvider, int button, boolean hasShiftDown, ItemStack heldItem) {
    getEntity().provideComponent().getContainerProvider().setChanged();
  }
}
