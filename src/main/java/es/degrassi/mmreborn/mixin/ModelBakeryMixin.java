package es.degrassi.mmreborn.mixin;

import es.degrassi.mmreborn.client.model.BuiltInModelHooks;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

  @Inject(at = @At("HEAD"), method="getModel", cancellable = true)
  private void getModelHook(ResourceLocation id, CallbackInfoReturnable<UnbakedModel> cir) {
    var model = BuiltInModelHooks.getBuiltInModel(id);
    if (model != null) {
      cir.setReturnValue(model);
    }
  }
}
