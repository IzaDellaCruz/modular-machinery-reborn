package es.degrassi.mmreborn.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.util.EnergyDisplayUtil;
import es.degrassi.mmreborn.common.block.prop.ConfigLoaded;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.MachineJsonReloadListener;
import es.degrassi.mmreborn.common.network.server.SOpenFilePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class MMRCommand {
  private MMRCommand() {}
  public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
    return Commands.literal(name)
      .then(logging())
      .then(reload());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> logging() {
    return Commands.literal("log")
      .requires(cs -> cs.hasPermission(2))
      .executes(ctx -> {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player)
          PacketDistributor.sendToPlayer(player, new SOpenFilePacket(new File("logs/modular_machinery_reborn/mmr.log").toURI().toString()));
        return 1;
      });
  }

  private static ArgumentBuilder<CommandSourceStack, ?> reload() {
    return Commands.literal("reload")
      .requires(cs -> cs.hasPermission(2))
      .executes(ctx -> {
        Config.load();
        ConfigLoaded.load();
        EnergyDisplayUtil.loadFromConfig();
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
          reloadMachines(player.server, player);
        }
        return 1;
      });
  }

  public static void reloadMachines(MinecraftServer server, @Nullable ServerPlayer player) {
    new MachineJsonReloadListener().reload(CompletableFuture::completedFuture, server.getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, server, server)
      .thenRun(() -> {
        if (player != null)
          player.sendSystemMessage(Component.translatable(ModularMachineryReborn.MODID + ".command.reload.machines").withStyle(ChatFormatting.GRAY));
      })
      .thenRun(() ->
          ModularMachineryReborn.CONTROLLERS.forEach(MachineControllerEntity::onStructureUnformed)
      );
  }
}
