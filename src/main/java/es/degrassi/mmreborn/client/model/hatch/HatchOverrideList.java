package es.degrassi.mmreborn.client.model.hatch;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HatchOverrideList extends ItemOverrides {

  @Override
  public @Nullable BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
    if (!(model instanceof DefaultHatchBakedModel machineModel))
      return super.resolve(model, stack, level, entity, seed);
    return machineModel.getMachineItemModel(stack);
  }
}
