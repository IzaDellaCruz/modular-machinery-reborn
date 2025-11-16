package es.degrassi.mmreborn.client.container;

import es.degrassi.mmreborn.client.ModularMachineryRebornClient;
import es.degrassi.mmreborn.common.entity.RedstonePortEntity;
import es.degrassi.mmreborn.common.registration.ContainerRegistration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class RedstonePortContainer extends ContainerBase<RedstonePortEntity> {
  public static void open(ServerPlayer player, RedstonePortEntity machine) {
    player.openMenu(new MenuProvider() {
      @Override
      public @NotNull Component getDisplayName() {
        return Component.translatable("modular_machinery_reborn.gui.title.redstone_port");
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new RedstonePortContainer(id, inv, machine);
      }
    }, buf -> buf.writeBlockPos(machine.getBlockPos()));
  }

  public RedstonePortContainer(int id, Inventory playerInv, RedstonePortEntity entity) {
    super(entity, playerInv.player, ContainerRegistration.REDSTONE_PORT.get(), id);
  }

  public RedstonePortContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
    this(id, inv, ModularMachineryRebornClient.getClientSideRedstonePortEntity(buffer.readBlockPos()));
  }
}
