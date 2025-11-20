package es.degrassi.mmreborn.common.integration.jade;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.crafting.helper.CraftingStatus;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class DynamicMachineComponentProvider implements IBlockComponentProvider {

  public static final DynamicMachineComponentProvider INSTANCE = new DynamicMachineComponentProvider();
  public static final ResourceLocation ID = ModularMachineryReborn.rl("machine_component_provider");

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    IElementHelper helper = IElementHelper.get();
    if (accessor.getBlockEntity() instanceof MachineControllerEntity) {
      CompoundTag tag = accessor.getServerData().getCompound(ModularMachineryReborn.MODID + ".controller");
      if (tag.isEmpty()) return;
      if (tag.contains("paused")) {
        tooltip.add(
            Component.translatable("gui.controller.status.paused").withStyle(ChatFormatting.DARK_RED)
        );
        return;
      }
      if (tag.contains("status", Tag.TAG_COMPOUND)) {
        CraftingStatus status = CraftingStatus.deserialize(tag.getCompound("status"), accessor.getLevel().registryAccess());
        MutableComponent message = status.getUnlocMessage().copy();
        switch (status.getStatus()) {
          case CRAFTING -> message.withStyle(ChatFormatting.GREEN);
          case NO_RECIPE -> message.withStyle(ChatFormatting.GOLD);
          case MISSING_STRUCTURE, FAILURE -> message.withStyle(ChatFormatting.RED);
        }
        tooltip.add(message);
      }
      if (tag.contains("runningCores", Tag.TAG_LONG)) {
        long runningCores = tag.getLong("runningCores");
        Component component = Component.translatable(
            "mmr.waila.cores",
            runningCores
        );
        tooltip.add(component);
      }
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ID;
  }
}
