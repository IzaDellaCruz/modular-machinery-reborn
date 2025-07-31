package es.degrassi.mmreborn.client.screen.widget;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoreActionButton extends ItemOrIconButton {

  private final CoreActionType type;

  public interface OnPressT {
    void onPress(CoreActionType type);
  }

  public CoreActionButton(int x, int y, OnPressT onPress, CoreActionType type) {
    super(x, y, type.icon(), btn -> onPress.onPress(type));
    this.type = type;
  }

  public CoreActionButton(OnPressT onPress, CoreActionType type) {
    this(0, 0, onPress, type);
  }

  @Override
  public @Nullable Tooltip getTooltip() {
    return Tooltip.create(type.component(Screen.hasShiftDown(), Screen.hasControlDown()));
  }

  @Override
  public List<Component> getTooltipMessage() {
    List<Component> components = Lists.newArrayList(type.component(Screen.hasShiftDown(), Screen.hasControlDown()));
    components.add(
        Component.translatable(
            "%s %s",
            Component.translatable("modular_machinery_reborn.controller.shift").withStyle(ChatFormatting.AQUA),
            type.component(true, false).copy().withStyle(ChatFormatting.GRAY)
        )
    );
    components.add(
        Component.translatable(
            "%s %s",
            Component.translatable("modular_machinery_reborn.controller.control").withStyle(ChatFormatting.AQUA),
            type.component(false, true).copy().withStyle(ChatFormatting.GRAY)
        )
    );
    return components;
  }
}
