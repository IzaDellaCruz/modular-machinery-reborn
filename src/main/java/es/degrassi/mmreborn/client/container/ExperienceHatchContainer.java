package es.degrassi.mmreborn.client.container;

import es.degrassi.mmreborn.client.ModularMachineryRebornClient;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.registration.ContainerRegistration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class ExperienceHatchContainer extends ContainerBase<ExperienceHatchEntity> {
  public static void open(ServerPlayer player, ExperienceHatchEntity machine) {
    player.openMenu(new MenuProvider() {
      @Override
      public @NotNull Component getDisplayName() {
        return machine.getBlockState().getBlock().getName();
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ExperienceHatchContainer(machine, player, id);
      }
    }, buf -> buf.writeBlockPos(machine.getBlockPos()));
  }
  public ExperienceHatchContainer(ExperienceHatchEntity entity, Player player, int containerId) {
    super(entity, player, ContainerRegistration.EXPERIENCE_HATCH.get(), containerId);
  }

  public ExperienceHatchContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
    this(ModularMachineryRebornClient.getClientSideExperienceHatchEntity(buffer.readBlockPos()), inv.player, id);
  }

  @Override
  public void init() {
    super.init();
    addSyncedSlot(new SlotItemComponent(
        getEntity().getCapabilityInventory().getInventory().get(0),
        new AtomicInteger(this.getFirstComponentSlotIndex()).getAndIncrement(),
        176 / 2 - 8,
        47
    ));
  }
}
