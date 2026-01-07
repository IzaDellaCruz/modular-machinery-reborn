package es.degrassi.mmreborn.common.network.client.emi;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class FillRecipeC2SPacket implements CustomPacketPayload {
  public static final Type<FillRecipeC2SPacket> TYPE = new Type<>(ModularMachineryReborn.rl("fill_recipe_mmr"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FillRecipeC2SPacket> CODEC = StreamCodec.ofMember(FillRecipeC2SPacket::write, FillRecipeC2SPacket::new);

  private final int syncId;
  private final int action;
  private final List<Integer> slots, crafting;
  private final int output;
  private final List<ItemStack> stacks;

  public FillRecipeC2SPacket(AbstractContainerMenu handler, int action, List<Slot> slots, List<Slot> crafting, @Nullable Slot output, List<ItemStack> stacks) {
    this.syncId = handler.containerId;
    this.action = action;
    this.slots = slots.stream().map(s -> s == null ? -1 : s.index).toList();
    this.crafting = crafting.stream().map(s -> s == null ? -1 : s.index).toList();
    this.output = output == null ? -1 : output.index;
    this.stacks = stacks;
  }

  public FillRecipeC2SPacket(RegistryFriendlyByteBuf buf) {
    syncId = buf.readInt();
    action = buf.readByte();
    slots = parseCompressedSlots(buf);
    crafting = Lists.newArrayList();
    int craftingSize = buf.readVarInt();
    for (int i = 0; i < craftingSize; i++) {
      int s = buf.readVarInt();
      crafting.add(s);
    }
    if (buf.readBoolean()) {
      output = buf.readVarInt();
    } else {
      output = -1;
    }
    int size = buf.readVarInt();
    stacks = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      stacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }
  }

  public void write(RegistryFriendlyByteBuf buf) {
    buf.writeInt(syncId);
    buf.writeByte(action);
    writeCompressedSlots(slots, buf);
    buf.writeVarInt(crafting.size());
    for (Integer s : crafting) {
      buf.writeVarInt(s);
    }
    if (output != -1) {
      buf.writeBoolean(true);
      buf.writeVarInt(output);
    } else {
      buf.writeBoolean(false);
    }
    buf.writeVarInt(stacks.size());
    for (ItemStack stack : stacks) {
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
    }
  }

  public void apply(Player player) {
    if (slots == null || crafting == null) {
      ModularMachineryReborn.LOGGER.error("Client requested fill but passed input and crafting slot information was invalid, aborting");
      return;
    }
    AbstractContainerMenu h = player.containerMenu;
    if (!(h instanceof ControllerContainer handler)) {
      ModularMachineryReborn.LOGGER.warn("Client requested fill but screen handler has changed, aborting");
      return;
    }
    if (handler.containerId != syncId) {
      ModularMachineryReborn.LOGGER.warn("Client requested fill but screen handler has changed, aborting");
      return;
    }
    handler.getEntity().getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_ITEM.get(), IOType.INPUT)
        .map(c -> (ItemComponent) c)
        .map(ItemComponent::getContainerProvider)
        .ifPresent(inventory -> {
          List<Slot> slots = inventory.createSlots(player);
          List<Slot> crafting = inventory.createInventorySlots(Lists.newArrayList(), 0);
          AtomicReference<Slot> output = new AtomicReference<>(null);
          if (this.output != -1) {
            handler.getEntity().getComponentManager()
                .getComponent(ComponentRegistration.COMPONENT_ITEM.get(), IOType.OUTPUT)
                .map(c -> (ItemComponent) c)
                .map(ItemComponent::getContainerProvider)
                .ifPresent(outInv -> {
                  if (this.output >= 0 && this.output < outInv.getSlots()) {
                    output.set(outInv.createInventorySlots(Lists.newArrayList(), 0).get(this.output));
                  }
                });
          }

          if (crafting.size() >= stacks.size()) {
            List<ItemStack> rubble = Lists.newArrayList();
            for (Slot slot : crafting) {
              if (slot != null && !slot.getItem().isEmpty()) {
                rubble.add(slot.getItem().copy());
                inventory.setStackInSlot(slot.getSlotIndex(), ItemStack.EMPTY);
              }
            }
            try {
              for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                if (stack.isEmpty()) continue;
                int gotten = grabMatching(player, inventory, slots, rubble, crafting, stack);
                if (gotten != stack.getCount()) {
                  if (gotten > 0) {
                    stack.setCount(gotten);
                    player.getInventory().placeItemBackInInventory(stack);
                  }
                } else {
                  Slot slot = crafting.get(i);
                  if (slot != null && stack.getCount() <= inventory.getSlotLimit(slot.getSlotIndex())) {
                    inventory.setStackInSlot(slot.getSlotIndex(), stack);
                  } else {
                    player.getInventory().placeItemBackInInventory(stack);
                  }
                }
              }
              if (output.get() != null) {
                if (action == 1) {
                  handler.clicked(output.get().getSlotIndex(), 0, ClickType.PICKUP, player);
                } else if (action == 2) {
                  handler.clicked(output.get().getSlotIndex(), 0, ClickType.QUICK_MOVE, player);
                }
              }
            } finally {
              for (ItemStack stack : rubble) {
                player.getInventory().placeItemBackInInventory(stack);
              }
            }
          }
        });
  }

  private static List<Integer> parseCompressedSlots(FriendlyByteBuf buf) {
    List<Integer> list = Lists.newArrayList();
    int amount = buf.readVarInt();
    for (int i = 0; i < amount; i++) {
      int low = buf.readVarInt();
      int high = buf.readVarInt();
      if (low < 0) {
        return null;
      }
      for (int j = low; j <= high; j++) {
        list.add(j);
      }
    }
    return list;
  }

  private static void writeCompressedSlots(List<Integer> list, FriendlyByteBuf buf) {
    List<Consumer<FriendlyByteBuf>> postWrite = Lists.newArrayList();
    int groups = 0;
    int i = 0;
    while (i < list.size()) {
      groups++;
      int start = i;
      int startValue = list.get(start);
      while (i < list.size() && i - start == list.get(i) - startValue) {
        i++;
      }
      int end = i - 1;
      postWrite.add(b -> {
        b.writeVarInt(startValue);
        b.writeVarInt(list.get(end));
      });
    }
    buf.writeVarInt(groups);
    for (Consumer<FriendlyByteBuf> consumer : postWrite) {
      consumer.accept(buf);
    }
  }

  private static int grabMatching(Player player, ItemHandler inventory, List<Slot> slots,
                                  List<ItemStack> rubble,
                                  List<Slot> crafting,
                                  ItemStack stack) {
    int amount = stack.getCount();
    int grabbed = 0;
    for (int i = 0; i < rubble.size(); i++) {
      if (grabbed >= amount) {
        return grabbed;
      }
      ItemStack r = rubble.get(i);
      if (ItemStack.isSameItemSameComponents(stack, r)) {
        int wanted = amount - grabbed;
        if (r.getCount() <= wanted) {
          grabbed += r.getCount();
          rubble.remove(i);
          i--;
        } else {
          grabbed = amount;
          r.setCount(r.getCount() - wanted);
        }
      }
    }
    for (Slot s : slots) {
      if (grabbed >= amount) {
        return grabbed;
      }
      if (crafting.contains(s)) {
        continue;
      }
      ItemStack st = s.getItem();
      if (ItemStack.isSameItemSameComponents(stack, st)) {
        int wanted = amount - grabbed;
        ItemStack taken = st.copy();
        if (st.getCount() <= wanted) {
          grabbed += st.getCount();
          inventory.setStackInSlot(s.getSlotIndex(), ItemStack.EMPTY);
        } else {
          grabbed = amount;
          st.setCount(st.getCount() - wanted);
        }
      }
    }
    return grabbed;
  }

  @Override
  @NotNull
  public Type<FillRecipeC2SPacket> type() {
    return TYPE;
  }

  @Override
  public String toString() {
    return "FillRecipeC2SPacket{" +
        "syncId=" + syncId +
        ", action=" + action +
        ", slots=" + slots +
        ", crafting=" + crafting +
        ", output=" + output +
        ", stacks=" + stacks +
        '}';
  }

  public static void handle(FillRecipeC2SPacket packet, IPayloadContext context) {
    if (!context.flow().isServerbound()) {
      throw new IllegalArgumentException("Trying to handle serverbound packet on client: " + packet);
    }
    var player = context.player();
    context.enqueueWork(() -> packet.apply(player));
  }
}
