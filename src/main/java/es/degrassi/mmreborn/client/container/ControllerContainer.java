package es.degrassi.mmreborn.client.container;

import es.degrassi.mmreborn.api.network.syncable.IntegerSyncable;
import es.degrassi.mmreborn.client.ModularMachineryRebornClient;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.registration.ContainerRegistration;
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

import java.util.List;

public class ControllerContainer extends ContainerBase<MachineControllerEntity> {
  private int corePage = 1;
  private final Int2ObjectMap<List<MachineProcessorCore>> pages = new Int2ObjectArrayMap<>();
  public Int2ObjectMap<Component> dynamicTooltips = new Int2ObjectArrayMap<>();

  public static void open(ServerPlayer player, MachineControllerEntity machine) {
    player.openMenu(new MenuProvider() {
      @Override
      public Component getDisplayName() {
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
  }

  public ControllerContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
    this(id, inv, ModularMachineryRebornClient.getClientSideMachineControllerEntity(buffer.readBlockPos()));
  }

  @Override
  public void clicked(int slotId, int button, ClickType clickType, Player player) {
    super.clicked(slotId, button, clickType, player);
  }

  @Override
  public void init() {
    super.init();
    stuffToSync.add(IntegerSyncable.create(() -> corePage, i -> corePage = i));
  }

  public List<MachineProcessorCore> getPage() {
    return entity.getPages().get(corePage);
  }

  public int getCurrentCorePage() {
    return corePage;
  }

  public int getPagesNumber() {
    return entity.getPages().size();
  }

  public void setPage(int corePage) {
    this.corePage = corePage;
  }

  public ResourceLocation getId() {
    return getEntity().getFoundMachine().getRegistryName();
  }

  public void setTooltips(Int2ObjectArrayMap<Component> tooltips) {
    this.dynamicTooltips = tooltips;
  }
}
