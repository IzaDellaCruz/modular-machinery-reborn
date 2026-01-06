package es.degrassi.mmreborn.common.integration.emi.recipe;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Transformation;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;

public class MMRMultiblockCategory extends EmiRecipeCategory {
  private final DynamicMachine multiblock;
  public MMRMultiblockCategory(DynamicMachine multiblock, EmiStack stack) {
    super(multiblock.getRegistryName().withPrefix("/multiblock_preview/"), new EmiMultiblockCategoryIcon(stack));
    this.multiblock = multiblock;
  }

  @Override
  public Component getName() {
    return Component.literal("Preview: ").append(Component.literal(multiblock.getLocalizedName()));
  }

  public static class EmiMultiblockCategoryIcon implements EmiRenderable {
    private final EmiStack controller;
    public EmiMultiblockCategoryIcon(EmiStack controller) {
      this.controller = controller;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta) {
      controller.render(draw, x, y, delta);
      draw.pose().pushPose();
      Lighting.setupFor3DItems();
      var v = new Vector3f(x / 2f, y / 2f, 0).add(5, 5, 4).div(0.5f);
      draw.pose().pushTransformation(new Transformation(v, null, new Vector3f(0.5f), null));
      draw.renderItem(null, Minecraft.getInstance().level, Items.STRUCTURE_BLOCK.getDefaultInstance(), 0, 0, 0, 150);
      draw.pose().popPose();
    }
  }
}
