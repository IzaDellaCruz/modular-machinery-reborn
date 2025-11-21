package es.degrassi.mmreborn.common.integration.jade;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.integration.jade.elements.FuelElement;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;
import java.util.Optional;

public class MachineHatchComponentProvider implements IBlockComponentProvider {
  public static final MachineHatchComponentProvider INSTANCE = new MachineHatchComponentProvider();
  public static final ResourceLocation ID = ModularMachineryReborn.rl("hatch_component_provider");

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    IElementHelper helper = IElementHelper.get();
    if (accessor.getBlockEntity() instanceof ColorableMachineComponentEntity entity) {
      CompoundTag tag = accessor.getServerData().getCompound(ModularMachineryReborn.MODID + ".hatch");
      if (tag.isEmpty()) return;

      CompoundTag fuelTag = tag.getCompound("fuel");
      if (!fuelTag.isEmpty()) addFuelInfo(helper, tooltip, fuelTag);

      CompoundTag effectTag = tag.getCompound("effect");
      if (!effectTag.isEmpty()) addEffectInfo(helper, tooltip, effectTag);

      CompoundTag redstoneTag = tag.getCompound("redstone");
      if (!redstoneTag.isEmpty()) addRedstoneInfo(helper, tooltip, redstoneTag);

      CompoundTag autoTag = tag.getCompound("auto");
      if (!autoTag.isEmpty()) {
        boolean autoInput = autoTag.getBoolean("input");
        boolean autoOutput = autoTag.getBoolean("output");
        Component input = Component.translatable("mmr.tooltip.auto_input", Component.translatable("mmr.gui.tooltip.enabled." + autoInput).withStyle(ChatFormatting.AQUA));
        Component output = Component.translatable("mmr.tooltip.auto_output", Component.translatable("mmr.gui.tooltip.enabled." + autoOutput).withStyle(ChatFormatting.AQUA));
        if (entity instanceof IAutoInputEntity)
          tooltip.add(input);
        if (entity instanceof IAutoOutputEntity)
          tooltip.add(output);
      }
    }
  }

  private void addRedstoneInfo(IElementHelper helper, ITooltip tooltip, CompoundTag redstoneTag) {
    Component text = null;
    int amount = redstoneTag.getInt("power");
    Component power = Component.literal(amount + "").withStyle(ChatFormatting.RED);
    if (redstoneTag.getBoolean("emit")) {
      if (amount != 0)
        text = Component.translatable("mmr.tooltip.redstone.emit", power);
    } else {
      text = Component.translatable("mmr.tooltip.redstone.receive", power);
    }
    if (text != null) {
      tooltip.add(helper.item(new ItemStack(Items.REDSTONE), 0.5f));
      tooltip.append(helper.spacer(4, 0));
      tooltip.append(helper.text(text).translate(new Vec2(0, 1)));
    }
  }

  private void addEffectInfo(IElementHelper helper, ITooltip tooltip, CompoundTag effectTag) {
    var effect = MobEffectInstance.load(effectTag.getCompound("instance"));
    if (effect != null) {
      var stack = new ItemStack(Items.POTION);
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(PotionContents.getColor(List.of(effect))), List.of(effect)));

      tooltip.add(helper.item(stack, 0.5f));
      tooltip.append(helper.spacer(4, 0));
      tooltip.append(helper.text(Component.translatable(effect.getDescriptionId()).append(" ").append(effectTag.getString("level")).withStyle(ChatFormatting.DARK_AQUA)).translate(new Vec2(0, 1)));
      if (effectTag.getBoolean("giving"))
        if (effectTag.getBoolean("interdimensional"))
          tooltip.add(Component.translatable("mmr.tooltip.effect.interdimensional"));
        else
          tooltip.add(Component.translatable("mmr.tooltip.effect", Component.literal(effectTag.getInt("radius") + "").withStyle(ChatFormatting.AQUA)));
    }
  }

  private void addFuelInfo(IElementHelper helper, ITooltip tooltip, CompoundTag fuelTag) {
    long amount = fuelTag.getLong("amount");
    long capacity = fuelTag.getLong("capacity");
    var percent = amount * 1f / capacity;
    ChatFormatting currentColor = ChatFormatting.RED;
    if (percent >= 0.80f) currentColor = ChatFormatting.GREEN;
    else if (percent >= 0.30f) currentColor = ChatFormatting.GOLD;
    tooltip.add(new FuelElement(percent));
    tooltip.append(helper.spacer(4, 0));
    tooltip.append(helper.text(Component.translatable("tooltip.fuel_tank.tank", amount, capacity).withStyle(currentColor)).translate(new Vec2(0, 1)));
  }

  @Override
  public ResourceLocation getUid() {
    return ID;
  }
}
