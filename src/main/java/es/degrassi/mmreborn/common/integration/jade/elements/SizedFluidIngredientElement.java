package es.degrassi.mmreborn.common.integration.jade.elements;

import es.degrassi.mmreborn.common.util.CycleTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SizedFluidIngredientElement extends Element {
  private static final Vec2 DEFAULT_SIZE = new Vec2(16.0F, 16.0F);
  private final CycleTimer timer = new CycleTimer(() -> 1000, true);
  private final List<JadeFluidObject> fluids;
  private final float chance;
  private final SizedFluidIngredient ingredient;

  public SizedFluidIngredientElement(SizedFluidIngredient fluid, float chance) {
    Objects.requireNonNull(fluid);
    this.chance = chance;
    this.ingredient = fluid;
    this.fluids = Arrays.stream(fluid.ingredient().getStacks()).map(s -> JadeFluidObject.of(s.getFluid(), s.getAmount(), s.getComponentsPatch())).toList();
  }

  public Vec2 getSize() {
    return DEFAULT_SIZE;
  }

  public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
    timer.onDraw();
    Vec2 size = this.getCachedSize();
    DisplayHelper.INSTANCE.drawFluid(guiGraphics, x, y, timer.get(this.fluids), size.x, size.y, JadeFluidObject.bucketVolume());

    if (!(DisplayHelper.INSTANCE.opacity() < 0.5F)) {
      var font = Minecraft.getInstance().font;
      int color = IThemeHelper.get().theme().text.itemAmountColor();
      // Chance Rendering
      if (this.chance < 1f) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + 1.0F, y + 1.0F, 200.0F);
        guiGraphics.pose().translate(0, -size.y, 0);
        String s = DisplayHelper.INSTANCE.humanReadableNumber(this.chance * 100, "%", false, null);
        if (chance == 0f) s = "NC";
        boolean smaller = s.length() > 3;
        float scale = smaller ? 0.5F : 0.75F;
        int i = smaller ? 32 : 22;
        int j = smaller ? 23 : 13;
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, s, i - font.width(s), j, color, true);
        guiGraphics.pose().popPose();
      }

      // Amount rendering
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(x, y, 200.0F);
      String s = DisplayHelper.INSTANCE.humanReadableNumber(this.ingredient.amount(), "B", true, null);
      boolean smaller = s.length() > 3;
      int i = smaller ? 32 : 22;
      int j = smaller ? 23 : 13;
      guiGraphics.pose().scale(0.75F, 0.75F, 1.0F);
      guiGraphics.drawString(font, s, i - font.width(s), j, color, true);
      guiGraphics.pose().popPose();
    }
  }

  public @Nullable String getMessage() {
    return null;
  }
}
