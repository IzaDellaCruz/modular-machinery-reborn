package es.degrassi.mmreborn.common.util;


import es.degrassi.mmreborn.ModularMachineryReborn;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@Getter
public class EmptyRequirementType {
  public static final ResourceKey<Registry<EmptyRequirementType>> REGISTRY_KEY =
      ResourceKey.createRegistryKey(ModularMachineryReborn.rl("empty_requirement_type"));

  private final int uOffset, vOffset, width, height;
  private final EmiConsumer emiConsumer;
  private final JeiConsumer jeiConsumer;

  protected EmptyRequirementType(int uOffset, int vOffset, int width, int height, EmiConsumer emiConsumer, JeiConsumer jeiConsumer) {
    this.uOffset = uOffset;
    this.vOffset = vOffset;
    this.width = width;
    this.height = height;
    this.emiConsumer = emiConsumer;
    this.jeiConsumer = jeiConsumer;
  }

  public static EmptyRequirementType create(int uOffset, int vOffset, int width, int height, EmiConsumer emiConsumer, JeiConsumer jeiConsumer) {
    return new EmptyRequirementType(uOffset, vOffset, width, height, emiConsumer, jeiConsumer);
  }
  public ResourceLocation getId() {
    return ModularMachineryReborn.getEmptyRequirementTypeRegistrar().getKey(this);
  }
}
