package es.degrassi.mmreborn.client.container;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.api.network.syncable.IntegerSyncable;
import es.degrassi.mmreborn.client.ModularMachineryRebornClient;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipeHandler;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.registration.ContainerRegistration;
import es.degrassi.mmreborn.common.util.Mods;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ControllerContainer extends ContainerBase<MachineControllerEntity> {
  private int corePage = 1;
  private final Int2ObjectMap<List<MachineProcessorCore>> pages = new Int2ObjectArrayMap<>();

  public static void open(ServerPlayer player, MachineControllerEntity machine) {
    player.openMenu(new MenuProvider() {
      @Override
      public @NotNull Component getDisplayName() {
        return Component.translatable("modular_machinery_reborn.gui.title.controller");
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ControllerContainer(id, inv, machine);
      }
    }, buf -> buf.writeBlockPos(machine.getBlockPos()));
  }

  public ControllerContainer(int id, Inventory playerInv, MachineControllerEntity entity) {
    super(entity, playerInv.player, ContainerRegistration.CONTROLLER.get(), id);
    int maxCores = entity.getProcessor().getMaxCores();
    int pages = maxCores / 50;
    int rest = maxCores % 50;
    pages += rest > 0 ? 1 : 0;
    for (int i = 0; i < pages; i++) {
      List<MachineProcessorCore> cores = Lists.newArrayList();
      for (int j = i * 50; j < Math.min((i + 1) * 50, maxCores); j++)
        cores.add(entity.getProcessor().cores().get(j));
      this.pages.put(i + 1, cores);
    }
  }

  public ControllerContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
    this(id, inv, ModularMachineryRebornClient.getClientSideMachineControllerEntity(buffer.readBlockPos()));
  }

  @Override
  public void clicked(int slotId, int button, ClickType clickType, Player player) {
    super.clicked(slotId, button, clickType, player);
    if (Mods.isEMILoaded()) {
      List<Slot> prevSlots = this.slots.stream().toList();
      List<Slot> newSlots = MMREmiRecipeHandler.getSlots(this);
      this.slots.clear();
      newSlots.stream()
          .filter(Objects::nonNull)
          .forEach(this.slots::add);
      this.slots.clear();
      this.slots.addAll(prevSlots);
    }
  }

  @Override
  public void init() {
    super.init();
    stuffToSync.add(IntegerSyncable.create(() -> corePage, i -> corePage = i));
  }

  public List<MachineProcessorCore> getPage() {
    return pages.get(corePage);
  }

  public int getCurrentCorePage() {
    return corePage;
  }

  public int getPagesNumber() {
    return pages.size();
  }

  public void setPage(int corePage) {
    this.corePage = corePage;
  }

  public ResourceLocation getId() {
    return getEntity().getFoundMachine().getRegistryName();
  }
}
