package es.degrassi.mmreborn.common.integration.emi.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.platform.EmiClient;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiItemComponent;
import es.degrassi.mmreborn.common.integration.emi.MMREmiPlugin;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.network.client.emi.FillRecipeC2SPacket;
import es.degrassi.mmreborn.common.util.MMRLogger;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MMREmiRecipeHandler implements StandardRecipeHandler<ControllerContainer> {
  public static final Component NO_ITEMS = EmiPort.translatable("mmr.emi.no_items");
  private final DynamicMachine machine;

  private static final Map<ControllerContainer, List<Slot>> slots = Maps.newHashMap();

  public MMREmiRecipeHandler(DynamicMachine machine) {
    this.machine = machine;
  }

  public static List<Slot> getSlots(ControllerContainer handler) {
    return MMREmiRecipeHandler.slots.computeIfAbsent(handler, MMREmiRecipeHandler::createSlots);
  }

  private static List<Slot> createSlots(ControllerContainer handler) {
    AtomicInteger slot = new AtomicInteger(0);
    List<Slot> slots = Lists.newArrayList(handler.slots.stream().peek(s -> s.index = slot.getAndIncrement()).iterator());

    slots.addAll(handler.getEntity()
        .getComponentManager()
        .getItemComponent(IOType.INPUT)
        .map(ItemComponent::getContainerProvider)
        .map(inventory -> inventory.createSlots(handler.getPlayer()))
        .stream()
        .flatMap(List::stream)
        .toList()
    );

    return slots;
  }

  @Override
  public EmiPlayerInventory getInventory(AbstractContainerScreen<ControllerContainer> screen) {
    return new EmiPlayerInventory(getSlots(screen.getMenu())
        .stream()
        .map(Slot::getItem)
        .map(EmiStack::of)
        .toList());
  }

  public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ControllerContainer> context) {
    if (recipe.getInputs().isEmpty()) return false;
    return context.getInventory().canCraft(recipe);
  }

  @Override
  public List<Slot> getInputSources(ControllerContainer handler) {
    return getSlots(handler);
  }

  @Override
  public List<Slot> getCraftingSlots(ControllerContainer handler) {
    List<Slot> slots = getSlots(handler);
    return slots.subList(36, slots.size());
  }

  @Override
  public boolean craft(EmiRecipe r, EmiCraftContext<ControllerContainer> context) {
    if (!(r instanceof MMREmiRecipe recipe)) return false;
    List<ItemStack> stacks = getStacks(recipe, context.getScreen(), context.getAmount());
    if (!stacks.isEmpty()) {
      Minecraft.getInstance().setScreen(context.getScreen());
      if (!EmiClient.onServer) {
        return clientFill(recipe, context.getScreen(), stacks, context.getDestination());
      } else {
        sendFillRecipe(context.getScreen(), context.getScreenHandler().containerId,
            switch (context.getDestination()) {
          case NONE -> 0;
          case CURSOR -> 1;
          case INVENTORY -> 2;
        }, stacks, recipe);
      }
      return true;
    }
    return false;
  }

  public List<ItemStack> getStacks(MMREmiRecipe recipe, AbstractContainerScreen<ControllerContainer> screen, int amount) {
    List<ItemStack> stacks = Lists.newArrayList();
    try {
      ControllerContainer screenHandler = screen.getMenu();
      screenHandler.getEntity().getComponentManager()
          .getItemComponent(IOType.INPUT)
          .map(ItemComponent::getContainerProvider)
          .ifPresent(inventory -> {
            List<EmiIngredient> ingredients = recipe.getInputs();
            Player player = screenHandler.getPlayer();
            List<Slot> slots = inventory.createSlots(player);
            List<Slot> crafting = inventory.createInventorySlots(Lists.newArrayList(), 0);
            List<Integer> ss = getInputSources(screenHandler).stream().map(s -> s == null ? -1 : s.index).toList();
            List<Integer> cs = getCraftingSlots(screenHandler).stream().map(s -> s == null ? -1 : s.index).toList();
            List<DiscoveredItem> discovered = Lists.newArrayList();
            Object2IntMap<EmiStack> weightDivider = new Object2IntOpenHashMap<>();
            for (int i = 0; i < ingredients.size(); i++) {
              List<DiscoveredItem> d = Lists.newArrayList();
              EmiIngredient ingredient = ingredients.get(i);
              List<EmiStack> emiStacks = ingredient.getEmiStacks();
              if (ingredient.isEmpty()) {
                discovered.add(null);
                continue;
              }
              for (EmiStack stack : emiStacks) {
                slotLoop:
                for (Slot slot : slots) {
                  ItemStack is = slot.getItem();
                  if (EmiStack.of(is).isEqual(stack)) {
                    for (DiscoveredItem di : d) {
                      if (ItemStack.isSameItemSameComponents(is, di.stack)) {
                        di.amount += is.getCount();
                        continue slotLoop;
                      }
                    }
                    d.add(new DiscoveredItem(stack, is, is.getCount(), (int) ingredient.getAmount(),
                        is.getMaxStackSize()));
                  }
                }
              }
              DiscoveredItem biggest = null;
              for (DiscoveredItem di : d) {
                if (biggest == null) biggest = di;
                else {
                  int a = di.amount / (weightDivider.getOrDefault(di.ingredient, 0) + di.consumed);
                  int ba = biggest.amount / (weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
                  if (ba < a) {
                    biggest = di;
                  }
                }
              }
              if (biggest == null || i >= crafting.size()) return;
              Slot slot = crafting.get(i);
              if (slot == null) return;
              weightDivider.put(biggest.ingredient,
                  weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
              biggest.max = Math.min(biggest.max, slot.getItem().getMaxStackSize());
              discovered.add(biggest);
            }
            if (discovered.isEmpty()) return;

            List<DiscoveredItem> unique = Lists.newArrayList();
            outer:
            for (DiscoveredItem di : discovered) {
              if (di == null) continue;
              for (DiscoveredItem ui : unique) {
                if (ItemStack.isSameItemSameComponents(di.stack, ui.stack)) {
                  ui.consumed += di.consumed;
                  continue outer;
                }
              }
              unique.add(new DiscoveredItem(di.ingredient, di.stack, di.amount, di.consumed, di.max));
            }
            int maxAmount = Integer.MAX_VALUE;
            for (DiscoveredItem ui : unique) {
              if (!ui.catalyst()) {
                maxAmount = Math.min(maxAmount, Math.min(ui.amount / ui.consumed, ui.max));
              }
            }
            maxAmount = Math.min(maxAmount, amount + batchesAlreadyPresent(recipe, screen));
            if (maxAmount == 0) return;
            for (DiscoveredItem di : discovered) {
              if (di != null) {
                ItemStack is = di.stack.copy();
                int a = di.catalyst() ? di.consumed : di.consumed * maxAmount;
                is.setCount(a);
                stacks.add(is);
              } else stacks.add(ItemStack.EMPTY);
            }
          });
    } catch (Exception e) {
      MMRLogger.INSTANCE.error(e);
    }
    return stacks;
  }

  public int batchesAlreadyPresent(MMREmiRecipe recipe, AbstractContainerScreen<ControllerContainer> screen) {
    List<EmiIngredient> ingredients = recipe.getInputs();
    List<ItemStack> stacks = Lists.newArrayList();
    Slot output = getOutputSlot(screen.getMenu());
    if (output != null && !output.getItem().isEmpty() && !recipe.getOutputs().isEmpty() && !ItemStack.matches(output.getItem(), recipe.getOutputs().stream().filter(stack -> stack instanceof ItemEmiStack).toList().get(0).getItemStack())) {
      return 0;
    }
    for (Slot slot : getCraftingSlots(screen.getMenu())) {
      if (slot != null)
        stacks.add(slot.getItem());
      else
        stacks.add(ItemStack.EMPTY);
    }
    long amount = Long.MAX_VALUE;
    outer:
    for (int i = 0; i < ingredients.size(); i++) {
      EmiIngredient input = ingredients.get(i);
      if (input.isEmpty()) {
        if (stacks.get(i).isEmpty()) continue;
        return 0;
      }
      if (i >= stacks.size()) return 0;
      EmiStack es = EmiStack.of(stacks.get(i));
      for (EmiStack v : input.getEmiStacks()) {
        if (v.isEmpty()) continue;
        if (v.isEqual(es) && es.getAmount() >= v.getAmount()) {
          amount = Math.min(amount, es.getAmount() / v.getAmount());
          continue outer;
        }
      }
      return 0;
    }
    if (amount < Long.MAX_VALUE && amount > 0) {
      return (int) amount;
    }
    return 0;
  }

  @Override
  public boolean supportsRecipe(EmiRecipe recipe) {
    return recipe.getCategory() == MMREmiPlugin.categories.get(machine);
  }

  @Override
  public void render(EmiRecipe recipe, EmiCraftContext<ControllerContainer> context, List<Widget> widgets, GuiGraphics draw) {
    renderMissing(recipe, context.getInventory(), widgets, draw);
  }

  @ApiStatus.Internal
  public static void renderMissing(EmiRecipe recipe, EmiPlayerInventory inv, List<Widget> widgets, GuiGraphics draw) {
    RenderSystem.enableDepthTest();
    Map<EmiIngredient, Boolean> availableForCrafting = getAvailable(recipe, inv);
    outer:
    for (Widget w : widgets) {
      if (w instanceof EmiItemComponent sw) {
        EmiIngredient stack = sw.getIngredient();
        for (Map.Entry<EmiIngredient, Boolean> entry : availableForCrafting.entrySet()) {
          if (!sw.getRequirement().requirement().getMode().isInput()) continue outer;
          if (stack.isEmpty()) continue outer;
          if (!EmiIngredient.areEqual(stack, entry.getKey())) continue;
          if (!entry.getValue()) {
            draw.fill(sw.getX() + 1, sw.getY() + 1, sw.getX() + 1 + sw.getWidth(), sw.getY() + 1 + sw.getHeight(), 200, 0x44FF0000);
            continue outer;
          }
        }
      }
    }
  }

  private static Map<EmiIngredient, Boolean> getAvailable(EmiRecipe recipe, EmiPlayerInventory inventory) {
    Map<EmiIngredient, Boolean> availableForCrafting = new IdentityHashMap<>();
    List<EmiIngredient> inputs = recipe.getInputs();
    List<Boolean> list = getCraftAvailability(inventory.inventory, inputs);
    if (list.size() != inputs.size()) {
      return Map.of();
    }
    for (int i = 0; i < list.size(); i++) {
      availableForCrafting.put(inputs.get(i), list.get(i));
    }
    return availableForCrafting;
  }

  public static List<Boolean> getCraftAvailability(Map<EmiStack, EmiStack> inventory, List<EmiIngredient> inputs) {
    Object2LongMap<EmiStack> used = new Object2LongOpenHashMap<>();
    List<Boolean> states = Lists.newArrayList();
    outer:
    for (EmiIngredient ingredient : inputs) {
      for (EmiStack stack : ingredient.getEmiStacks()) {
        long desired = stack.getAmount();
        if (inventory.containsKey(stack)) {
          EmiStack identity = inventory.get(stack);
          long alreadyUsed = used.getOrDefault(identity, 0);
          long available = identity.getAmount() - alreadyUsed;
          if (available >= desired) {
            used.put(identity, desired + alreadyUsed);
            states.add(true);
            continue outer;
          }
        }
      }
      states.add(false);
    }
    return states;
  }

  public void sendFillRecipe(AbstractContainerScreen<ControllerContainer> screen,
                                                                      int syncId, int action, List<ItemStack> stacks,
                                                                      MMREmiRecipe recipe) {
    ControllerContainer screenHandler = screen.getMenu();
    List<Slot> crafting = getCraftingSlots(recipe, screenHandler);
    Slot output = getOutputSlot(screenHandler);
    PacketDistributor.sendToServer(new FillRecipeC2SPacket(screenHandler, action, getInputSources(screenHandler), crafting, output, stacks));
  }

  public boolean clientFill(MMREmiRecipe recipe, AbstractContainerScreen<ControllerContainer> screen, List<ItemStack> stacks, EmiCraftContext.Destination destination) {
    ControllerContainer screenHandler = screen.getMenu();
    if (screenHandler.getCarried().isEmpty()) {
      Minecraft client = Minecraft.getInstance();
      MultiPlayerGameMode manager = client.gameMode;
      Player player = client.player;
      List<Slot> clear = getCraftingSlots(screenHandler);
      for (Slot slot : clear) {
        if (slot != null) {
          manager.handleInventoryMouseClick(screenHandler.containerId, slot.index, 0, ClickType.QUICK_MOVE, player);
        }
      }
      List<Slot> inputs = getInputSources(screenHandler);
      List<Slot> slots = getCraftingSlots(recipe, screenHandler);
      outer:
      for (int i = 0; i < stacks.size(); i++) {
        ItemStack stack = stacks.get(i);
        if (stack.isEmpty()) {
          continue;
        }
        if (i >= slots.size()) {
          return false;
        }
        Slot crafting = slots.get(i);
        if (crafting == null) {
          return false;
        }
        int needed = stack.getCount();
        for (Slot input : inputs) {
          if (slots.contains(input)) {
            continue;
          }
          ItemStack is = input.getItem().copy();
          if (ItemStack.isSameItemSameComponents(is, stack)) {
            manager.handleInventoryMouseClick(screenHandler.containerId, input.index, 0, ClickType.PICKUP, player);
            if (is.getCount() <= needed) {
              needed -= is.getCount();
              manager.handleInventoryMouseClick(screenHandler.containerId, crafting.index, 0, ClickType.PICKUP, player);
            } else {
              while (needed > 0) {
                manager.handleInventoryMouseClick(screenHandler.containerId, crafting.index, 1, ClickType.PICKUP, player);
                needed--;
              }
              manager.handleInventoryMouseClick(screenHandler.containerId, input.index, 0, ClickType.PICKUP, player);
            }
          }
          if (needed == 0) {
            continue outer;
          }
        }
        return false;
      }
      Slot slot = getOutputSlot(screenHandler);
      if (slot != null) {
        if (destination == EmiCraftContext.Destination.CURSOR) {
          manager.handleInventoryMouseClick(screenHandler.containerId, slot.index, 0, ClickType.PICKUP, player);
        } else if (destination == EmiCraftContext.Destination.INVENTORY) {
          manager.handleInventoryMouseClick(screenHandler.containerId, slot.index, 0, ClickType.QUICK_MOVE, player);
        }
      }
      return true;
    }
    return false;
  }

  public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<ControllerContainer> context) {
    if (canCraft(recipe, context)) {
      return List.of();
    } else {
      if (!recipe.getInputs().isEmpty())
        return List.of(ClientTooltipComponent.create(EmiPort.ordered(NOT_ENOUGH_INGREDIENTS)));
      return List.of(ClientTooltipComponent.create(EmiPort.ordered(NO_ITEMS)));
    }
  }

  private static class DiscoveredItem {
    private static final Comparison COMPARISON = Comparison.DEFAULT_COMPARISON;
    public EmiStack ingredient;
    public ItemStack stack;
    public int consumed;
    public int amount;
    public int max;

    public DiscoveredItem(EmiStack ingredient, ItemStack stack, int amount, int consumed, int max) {
      this.ingredient = ingredient;
      this.stack = stack.copy();
      this.amount = amount;
      this.consumed = consumed;
      this.max = max;
    }

    public boolean catalyst() {
      return ingredient.getRemainder().isEqual(ingredient, COMPARISON);
    }
  }
}
