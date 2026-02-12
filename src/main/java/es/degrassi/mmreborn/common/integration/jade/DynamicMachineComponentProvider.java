package es.degrassi.mmreborn.common.integration.jade;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.crafting.helper.CraftingStatus;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.integration.jade.elements.SizedFluidIngredientElement;
import es.degrassi.mmreborn.common.integration.jade.elements.SizedItemIngredientElement;
import es.degrassi.mmreborn.common.util.MMRLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;

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
        tooltip.add(Component.translatable("gui.controller.status.paused").withStyle(ChatFormatting.DARK_RED));
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
        if (runningCores == 1) {
          var progress = tag.getCompound("progress");
          var outputs = tag.getCompound("outputs");
          var items = outputs.getList("item", Tag.TAG_COMPOUND);
          var fluids = outputs.getList("fluid", Tag.TAG_COMPOUND);
          var current = progress.getFloat("current");
          var total = progress.getFloat("total");
          tooltip.add(helper.progress(
              current / total,
              Component.translatable("%s/%s", (int)current, (int)total),
              helper.progressStyle().textColor(ChatFormatting.WHITE.getColor()),
              BoxStyle.getNestedBox(),
              true
          ));
          tooltip.add(Component.literal("Outputs:").withStyle(ChatFormatting.GRAY));
          List<IElement> itemElements = Lists.newArrayList();
          items.stream()
              .map(t -> (CompoundTag) t)
              .forEach(t -> {
                  SizedIngredient.FLAT_CODEC.decode(NbtOps.INSTANCE, t.getCompound("ingredient")).resultOrPartial().map(Pair::getFirst).ifPresent(ing -> {
                    var chance = t.getFloat("chance");
                    itemElements.add(SizedItemIngredientElement.of(ing, chance));
                  });
              });
          var size = tooltip.size();
          tooltip.add(itemElements);
          List<IElement> fluidElements = Lists.newArrayList();
          fluids.stream()
              .map(t -> (CompoundTag) t)
              .forEach(t -> {
                SizedFluidIngredient.FLAT_CODEC.decode(NbtOps.INSTANCE, t.getCompound("ingredient")).resultOrPartial().map(Pair::getFirst).ifPresent(ing -> {
                  var chance = t.getFloat("chance");
                  fluidElements.add(new SizedFluidIngredientElement(ing, chance));
                });
              });
          tooltip.add(fluidElements);
        } else {
          Component component = Component.translatable(
              "mmr.waila.cores",
              runningCores
          );
          tooltip.add(component);
        }
      }
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ID;
  }
}
