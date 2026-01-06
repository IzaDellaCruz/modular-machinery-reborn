package es.degrassi.mmreborn.common.integration.jei.category;

import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Transformation;
import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import es.degrassi.mmreborn.common.integration.xei.MultiblockRecipe;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;

public class MMRMultiblockRecipeCategory extends ModularUIRecipeCategory<MultiblockRecipe> {
  private final DynamicMachine multiblock;
  @Getter
  private final RecipeType<MultiblockRecipe> recipeType;
  @Getter
  private final IDrawable icon;
  public MMRMultiblockRecipeCategory(DynamicMachine multiblock) {
    super(MultiblockRecipe::createModularUI);
    this.multiblock = multiblock;
    var machine = multiblock.getRegistryName().withPrefix("multiblock_preview/");
    this.recipeType = new RecipeType<>(machine, MultiblockRecipe.class);
    this.icon = new JeiMultiblockCategoryIcon(multiblock);
  }

  public int getWidth() {
    return MultiblockRecipe.WIDTH;
  }

  public int getHeight() {
    return MultiblockRecipe.HEIGHT;
  }

  @Override
  public Component getTitle() {
    return Component.literal("Preview: ").append(multiblock.getName());
  }

  public static class JeiMultiblockCategoryIcon implements IDrawable {
    private final IDrawable controller;
    public JeiMultiblockCategoryIcon(DynamicMachine multiblock) {
      this.controller = MMRJeiPlugin.jeiHelpers.getGuiHelper().createDrawableItemStack(ControllerItem.makeMachineItem(multiblock.getRegistryName()));
    }

    @Override
    public int getWidth() {
      return 16;
    }

    @Override
    public int getHeight() {
      return 16;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
      controller.draw(guiGraphics, xOffset, yOffset);
      guiGraphics.pose().pushPose();
      Lighting.setupFor3DItems();
      var v = new Vector3f(xOffset / 2f, yOffset / 2f, 0).add(5, 5, 4).div(0.5f);
      guiGraphics.pose().pushTransformation(new Transformation(v, null, new Vector3f(0.5f), null));
      guiGraphics.renderItem(null, Minecraft.getInstance().level, Items.STRUCTURE_BLOCK.getDefaultInstance(), 0, 0, 0, 150);
      guiGraphics.pose().popPose();
    }
  }
}
