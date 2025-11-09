package es.degrassi.mmreborn.data.blockstate.builder;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.data.MMRTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.IGeneratedBlockState;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class MMRStateBuilder<T extends MMRStateBuilder<T>> implements IGeneratedBlockState {
  private TagKey<Block> connectTo;

  private ResourceLocation center;
  private ResourceLocation empty;
  private ResourceLocation horizontal;
  private ResourceLocation particle;
  private ResourceLocation vertical;
  private ResourceLocation overlay = null;
  private ResourceLocation ovCenter = null;
  private ResourceLocation ovVertical = null;
  private ResourceLocation ovHorizontal = null;
  private ResourceLocation ovParticle = null;

  protected abstract ResourceLocation loader();

  @Override
  public @NotNull JsonObject toJson() throws IllegalArgumentException {
    JsonObject blockState = new JsonObject();
    JsonObject variants = new JsonObject();
    JsonObject empty = new JsonObject();
    empty.addProperty("model", "minecraft:block/air");
    variants.add("", empty);
    blockState.add("variants", variants);
    blockState.addProperty("athena:loader", loader().toString());
    addCtmTextures(blockState);
    addConnectTo(blockState);
    return blockState;
  }

  private void addCtmTextures(JsonObject blockState) {
    if (center == null) throw new NullTextureException("Center");
    if (empty == null) throw new NullTextureException("Empty");
    if (horizontal == null) throw new NullTextureException("Horizontal");
    if (particle == null) throw new NullTextureException("Particle");
    if (vertical == null) throw new NullTextureException("Vertical");
    JsonObject ctmTextures = new JsonObject();
    ctmTextures.addProperty("center", center.toString());
    ctmTextures.addProperty("empty", empty.toString());
    ctmTextures.addProperty("horizontal", horizontal.toString());
    ctmTextures.addProperty("particle", particle.toString());
    ctmTextures.addProperty("vertical", vertical.toString());
    if (overlay != null)
      ctmTextures.addProperty("overlay", overlay.toString());
    if (ovCenter != null)
      ctmTextures.addProperty("ov_center", ovCenter.toString());
    if (ovVertical != null)
      ctmTextures.addProperty("ov_vertical", ovVertical.toString());
    if (ovHorizontal != null)
      ctmTextures.addProperty("ov_horizontal", ovHorizontal.toString());
    if (ovParticle != null)
      ctmTextures.addProperty("ov_particle", ovParticle.toString());
    blockState.add("ctm_textures", ctmTextures);
  }

  private void addConnectTo(JsonObject blockState) {
    if (connectTo == null) throw new IllegalArgumentException("Connect to can not be null");
    JsonObject tag = new JsonObject();
    tag.addProperty("type", "tag");
    tag.addProperty("tag", connectTo());
    blockState.add("connect_to", tag);
  }

  private @NotNull String connectTo() {
    return connectTo.location().toString();
  }

  private T self() {
    return (T) this;
  }

  public T connectToPlain() {
    connectTo = MMRTags.Blocks.PLAIN_CONNECTABLE;
    return self();
  }

  public T connectToReinforced() {
    connectTo = MMRTags.Blocks.REINFORCED_CONNECTABLE;
    return self();
  }

  public T center(ResourceLocation center) {
    this.center = center;
    return self();
  }

  public T empty(ResourceLocation empty) {
    this.empty = empty;
    return self();
  }

  public T horizontal(ResourceLocation horizontal) {
    this.horizontal = horizontal;
    return self();
  }

  public T particle(ResourceLocation particle) {
    this.particle = particle;
    return self();
  }

  public T vertical(ResourceLocation vertical) {
    this.vertical = vertical;
    return self();
  }

  public T ovCenter(ResourceLocation ovCenter) {
    this.ovCenter = ovCenter;
    return self();
  }

  public T ovVertical(ResourceLocation ovVertical) {
    this.ovVertical = ovVertical;
    return self();
  }

  public T ovHorizontal(ResourceLocation ovHorizontal) {
    this.ovHorizontal = ovHorizontal;
    return self();
  }

  public T ovParticle(ResourceLocation ovParticle) {
    this.ovParticle = ovParticle;
    return self();
  }

  public T overlay(ResourceLocation overlay) {
    this.overlay = overlay;
    return self();
  }

  private static class NullTextureException extends IllegalArgumentException {
    private NullTextureException(String texture) {
      super(texture + " texture can not be null");
    }
  }
}
