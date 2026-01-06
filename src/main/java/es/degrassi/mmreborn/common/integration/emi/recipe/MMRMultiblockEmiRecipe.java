package es.degrassi.mmreborn.common.integration.emi.recipe;

import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import es.degrassi.mmreborn.common.integration.xei.MultiblockRecipe;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MMRMultiblockEmiRecipe extends ModularUIEMIRecipe {
  @Getter
  protected MMRMultiblockCategory category;
  protected MultiblockRecipe multiblock;
  public MMRMultiblockEmiRecipe(MultiblockRecipe multiblock, MMRMultiblockCategory category) {
    super(recipe -> multiblock.createModularUI());
    this.category = category;
    this.multiblock = multiblock;
  }

  @Override
  public @Nullable ResourceLocation getId() {
    return multiblock.getId();
  }

  @Override
  public int getDisplayWidth() {
    return MultiblockRecipe.WIDTH;
  }

  @Override
  public int getDisplayHeight() {
    return MultiblockRecipe.HEIGHT;
  }
}
