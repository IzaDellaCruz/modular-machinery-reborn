package es.degrassi.mmreborn.common.integration.jade.elements;

import es.degrassi.mmreborn.common.util.CycleTimer;
import es.degrassi.mmreborn.common.util.MMRLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

import java.util.Arrays;
import java.util.List;

public class SizedItemIngredientElement extends Element {
  private final SizedIngredient item;
  private final float scale;
  private final CycleTimer timer = new CycleTimer(() -> 1000, true);
  public static final SizedItemIngredientElement EMPTY = new SizedItemIngredientElement(new SizedIngredient(Ingredient.EMPTY, 1), 1, 0.5f);
  private final List<ItemStack> itemList;
  private final float chance;

  private SizedItemIngredientElement(SizedIngredient item, float chance, float scale) {
    this.item = item;
    this.scale = scale;
    this.chance = chance;
    this.itemList = Arrays.stream(this.item.getItems()).map(s -> s.copyWithCount(item.count())).toList();
  }

  public static SizedItemIngredientElement of(SizedIngredient stack, float chance) {
    return of(stack, chance, 1.0F);
  }
  public static SizedItemIngredientElement of(SizedIngredient stack, float chance, float scale) {
    return stack.ingredient().isEmpty() ? EMPTY : new SizedItemIngredientElement(stack, chance, scale);
  }

  public Vec2 getSize() {
    int size = Mth.floor(18.0F * this.scale);
    return new Vec2((float)size, (float)size);
  }

  public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
    timer.onDraw();
    var item = timer.getOrDefault(itemList, ItemStack.EMPTY);
    if (!(DisplayHelper.INSTANCE.opacity() < 0.5F)) {
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(x, y, 0.0F);
      guiGraphics.pose().scale(scale, scale, scale);
      guiGraphics.renderFakeItem(item, 0, 0);
      guiGraphics.renderItemDecorations(Minecraft.getInstance().font, item, 0, 0);
      guiGraphics.pose().popPose();
    }
    if (this.chance < 1f) {
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(x + 19 - 2, y, 200.0F);
      String s = DisplayHelper.INSTANCE.humanReadableNumber(this.chance * 100, "%", false, null);
      if (chance == 0f) s = "NC";
      boolean smaller = s.length() > 3;
      float scale = 0.75F;
      int i = smaller ? 32 : 22;
      int j = smaller ? 23 : 13;
      guiGraphics.pose().scale(scale, scale, 1.0F);
      var font = Minecraft.getInstance().font;
      int color = IThemeHelper.get().theme().text.colors().info();
      guiGraphics.drawString(font, s, -font.width(s), 0, color, true);
      guiGraphics.pose().popPose();
    }
  }

  public @Nullable String getMessage() {
    return null;
  }

  @Override
  public String toString() {
    return this.item.count() + "x " + Arrays.toString(this.item.ingredient().getItems());
  }
}
