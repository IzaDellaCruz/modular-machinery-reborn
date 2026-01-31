package es.degrassi.mmreborn.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public final class Blitter {

  // This assumption is obviously bogus, but currently all textures are this size,
  // and it's impossible to get the texture size from an already loaded texture.
  // The coordinates will still be correct when a resource pack provides bigger textures as long
  // as each texture element is still positioned at the same relative position
  public static final int DEFAULT_TEXTURE_WIDTH = 256;
  public static final int DEFAULT_TEXTURE_HEIGHT = 256;

  private final ResourceLocation texture;
  // This texture size is only used to convert the source rectangle into uv coordinates (which are [0,1] and work
  // with textures of any size at runtime).
  private final int referenceWidth;
  private final int referenceHeight;
  private int r = 255;
  private int g = 255;
  private int b = 255;
  private int a = 255;
  private Rect2i srcRect;
  private Rect2i destRect = new Rect2i(0, 0, 0, 0);
  private boolean blending = true;
  private TextureTransform transform = TextureTransform.NONE;
  private int zOffset;

  Blitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
    this.texture = texture;
    this.referenceWidth = referenceWidth;
    this.referenceHeight = referenceHeight;
  }

  /**
   * Creates a blitter where the source rectangle is in relation to a 256x256 pixel texture.
   */
  public static Blitter texture(ResourceLocation file) {
    return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
  }

  /**
   * Creates a blitter where the source rectangle is in relation to a 256x256 pixel texture.
   */
  public static Blitter texture(String file) {
    return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
  }

  /**
   * Creates a blitter where the source rectangle is in relation to a texture of the given size.
   */
  public static Blitter texture(ResourceLocation file, int referenceWidth, int referenceHeight) {
    return new Blitter(file, referenceWidth, referenceHeight);
  }

  /**
   * Creates a blitter where the source rectangle is in relation to a texture of the given size.
   */
  public static Blitter texture(String file, int referenceWidth, int referenceHeight) {
    return new Blitter(ModularMachineryReborn.rl("textures/" + file), referenceWidth, referenceHeight);
  }

  /**
   * Creates a blitter from a texture atlas sprite.
   */
  public static Blitter sprite(TextureAtlasSprite sprite) {
    var atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());

    return new Blitter(sprite.atlasLocation(), atlas.getWidth(), atlas.getHeight())
        .src(
            sprite.getX(),
            sprite.getY(),
            sprite.contents().width(),
            sprite.contents().height());
  }

  public static Blitter guiSprite(ResourceLocation resourceLocation) {
    var sprites = Minecraft.getInstance().getGuiSprites();
    var sprite = sprites.getSprite(resourceLocation);
    return sprite(sprite);
  }

  public Blitter copy() {
    Blitter result = new Blitter(texture, referenceWidth, referenceHeight);
    result.srcRect = srcRect;
    result.destRect = destRect;
    result.r = r;
    result.g = g;
    result.b = b;
    result.a = a;
    return result;
  }

  public int getSrcX() {
    return srcRect == null ? 0 : srcRect.getX();
  }

  public int getSrcY() {
    return srcRect == null ? 0 : srcRect.getY();
  }

  public int getSrcWidth() {
    return srcRect == null ? destRect.getWidth() : srcRect.getWidth();
  }

  public int getSrcHeight() {
    return srcRect == null ? destRect.getHeight() : srcRect.getHeight();
  }

  /**
   * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
   */
  public Blitter src(int x, int y, int w, int h) {
    this.srcRect = new Rect2i(x, y, w, h);
    return this;
  }

  public Blitter srcWidth(int w) {
    this.srcRect = new Rect2i(srcRect.getX(), srcRect.getY(), w, srcRect.getHeight());
    return this;
  }

  public Blitter srcHeight(int h) {
    this.srcRect = new Rect2i(srcRect.getX(), srcRect.getY(), srcRect.getWidth(), h);
    return this;
  }

  /**
   * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
   */
  public Blitter src(Rect2i rect) {
    return src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
  }

  /**
   * Draw into the rectangle defined by the given coordinates.
   */
  public Blitter dest(int x, int y, int w, int h) {
    this.destRect = new Rect2i(x, y, w, h);
    return this;
  }

  /**
   * Draw at the given x,y coordinate and use the source rectangle size as the destination rectangle size.
   */
  public Blitter dest(int x, int y) {
    return dest(x, y, 0, 0);
  }

  /**
   * Draw into the given rectangle.
   */
  public Blitter dest(Rect2i rect) {
    return dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
  }

  public Rect2i getDestRect() {
    int x = destRect.getX();
    int y = destRect.getY();
    int w = 0, h = 0;
    if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
      w = destRect.getWidth();
      h = destRect.getHeight();
    } else if (srcRect != null) {
      w = srcRect.getWidth();
      h = srcRect.getHeight();
    }
    return new Rect2i(x, y, w, h);
  }

  public Blitter color(float r, float g, float b) {
    this.r = (int) (Mth.clamp(r, 0, 1) * 255);
    this.g = (int) (Mth.clamp(g, 0, 1) * 255);
    this.b = (int) (Mth.clamp(b, 0, 1) * 255);
    return this;
  }

  public Blitter colorArgb(int packedArgb) {
    this.a = FastColor.ARGB32.alpha(packedArgb);
    this.r = FastColor.ARGB32.red(packedArgb);
    this.g = FastColor.ARGB32.green(packedArgb);
    this.b = FastColor.ARGB32.blue(packedArgb);
    return this;
  }

  public Blitter opacity(float a) {
    this.a = (int) (Mth.clamp(a, 0, 1) * 255);
    return this;
  }

  public Blitter color(float r, float g, float b, float a) {
    return color(r, g, b).opacity(a);
  }

  public Blitter transform(TextureTransform transform) {
    this.transform = Objects.requireNonNull(transform);
    return this;
  }

  /**
   * Enables or disables alpha-blending. If disabled, all pixels of the texture will be drawn as opaque, and the alpha
   * value set using {@link #opacity(float)} will be ignored.
   */
  public Blitter blending(boolean enable) {
    this.blending = enable;
    return this;
  }

  /**
   * Sets the color to the R,G,B values encoded in the lower 24-bit of the given integer.
   */
  public Blitter colorRgb(int packedRgb) {
    float r = (packedRgb >> 16 & 255) / 255.0F;
    float g = (packedRgb >> 8 & 255) / 255.0F;
    float b = (packedRgb & 255) / 255.0F;

    return color(r, g, b);
  }

  public Blitter zOffset(int offset) {
    this.zOffset = offset;
    return this;
  }

  public void blit(GuiGraphics guiGraphics) {
    RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    RenderSystem.setShaderTexture(0, this.texture);

    // With no source rectangle, we'll use the entirety of the texture. This happens rarely though.
    float minU, minV, maxU, maxV;
    if (srcRect == null) {
      minU = minV = 0;
      maxU = maxV = 1;
    } else {
      minU = srcRect.getX() / (float) referenceWidth;
      minV = srcRect.getY() / (float) referenceHeight;
      maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
      maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
    }

    // Transform the UV
    switch (transform) {
      case MIRROR_H -> {
        var tmp = minU;
        minU = maxU;
        maxU = tmp;
      }
      case MIRROR_V -> {
        var tmp = minV;
        minV = maxV;
        maxV = tmp;
      }
    }

    // It's possible to not set a destination rectangle size, in which case the
    // source rectangle size will be used
    float x1 = destRect.getX();
    float y1 = destRect.getY();
    float x2 = x1, y2 = y1;
    if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
      x2 += destRect.getWidth();
      y2 += destRect.getHeight();
    } else if (srcRect != null) {
      x2 += srcRect.getWidth();
      y2 += srcRect.getHeight();
    }

    Matrix4f matrix = guiGraphics.pose().last().pose();

    var bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
        DefaultVertexFormat.POSITION_TEX_COLOR);
    bufferbuilder.addVertex(matrix, x1, y2, zOffset)
        .setUv(minU, maxV)
        .setColor(r, g, b, a);
    bufferbuilder.addVertex(matrix, x2, y2, zOffset)
        .setUv(maxU, maxV)
        .setColor(r, g, b, a);
    bufferbuilder.addVertex(matrix, x2, y1, zOffset)
        .setUv(maxU, minV)
        .setColor(r, g, b, a);
    bufferbuilder.addVertex(matrix, x1, y1, zOffset)
        .setUv(minU, minV)
        .setColor(r, g, b, a);

    if (blending) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    } else {
      RenderSystem.disableBlend();
    }
    BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
  }

  public void blitWorld(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      Direction face,
      int packedOverlay
  ) {
    poseStack.pushPose();
    // Use the standard block cutout / translucent render type
    RenderType renderType = blending
        ? RenderType.entityTranslucent(texture)
        : RenderType.entityCutout(texture);

    VertexConsumer consumer = bufferSource.getBuffer(renderType);

    // UVs
    float minU, minV, maxU, maxV;
    if (srcRect == null) {
      minU = minV = 0;
      maxU = maxV = 1;
    } else {
      minU = srcRect.getX() / (float) referenceWidth;
      minV = srcRect.getY() / (float) referenceHeight;
      maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
      maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
    }

    // UV transform
    if (transform == TextureTransform.MIRROR_H) {
      float tmp = minU;
      minU = maxU;
      maxU = tmp;
    } else if (transform == TextureTransform.MIRROR_V) {
      float tmp = minV;
      minV = maxV;
      maxV = tmp;
    }

    float size = 12f / 16f; // 6x6 icon
    float margin = (1f - size) / 2f;
    float x2 = margin + size;
    float y2 = margin + size;

    switch (face) {
      case NORTH -> poseStack.translate(0, 0, -0.001);
      case SOUTH -> poseStack.translate(0, 0, 0.001);
      case WEST  -> poseStack.translate(-0.001, 0, 0);
      case EAST  -> poseStack.translate(0.001, 0, 0);
      case DOWN  -> poseStack.translate(0, -0.001, 0);
      case UP    -> poseStack.translate(0, 0.001, 0);
    }

    poseStack.translate(0.5, 0.5, 0.5);

    Vec3i n = face.getNormal();
    float nx = n.getX();
    float ny = n.getY();
    float nz = n.getZ();
    // rotate around face normal
    switch (face) {
      case UP, DOWN -> {
        var player = Minecraft.getInstance().player;
        if (player == null) break;
        var playerFacing = player.getDirection();
        float snappedYaw = switch (playerFacing) {
          case NORTH -> 180f;
          case WEST  -> 90f;
          case EAST  -> -90f;
          default -> 0f;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(-snappedYaw));
      }
      default -> {}
    }

    poseStack.translate(-0.5, -0.5, -0.5);

    PoseStack.Pose pose = poseStack.last();
    Matrix4f mat = pose.pose();

    switch (face) {
      case NORTH, SOUTH, EAST, WEST -> {
        float tmp = minV;
        minV = maxV;
        maxV = tmp;
      }
      case UP -> {
        float tempU = minU;
        minU = maxU;
        maxU = tempU;
        float tempV = minV;
        minV = maxV;
        maxV = tempV;
      }
      case DOWN -> {
        float tmp = minU;
        minU = maxU;
        maxU = tmp;
      }
    }

    // Emit quad per face
    emitFaceQuad(
        consumer, mat,
        face,
        margin, margin, x2, y2,
        minU, minV, maxU, maxV,
        nx, ny, nz,
        packedOverlay
    );
    poseStack.popPose();
  }

  private void emitFaceQuad(
      VertexConsumer vc,
      Matrix4f mat,
      Direction face,
      float x1, float y1, float x2, float y2,
      float u1, float v1, float u2, float v2,
      float nx, float ny, float nz,
      int packedOverlay
  ) {
    switch (face) {
      case NORTH -> {
        vertex(vc, mat, x1, y1, 0, u2, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, y1, 0, u1, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, y2, 0, u1, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x1, y2, 0, u2, v2, nx, ny, nz, packedOverlay);
      }
      case SOUTH -> {
        vertex(vc, mat, x2, y1, 1, u2, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x1, y1, 1, u1, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x1, y2, 1, u1, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, y2, 1, u2, v2, nx, ny, nz, packedOverlay);
      }
      case WEST -> {
        vertex(vc, mat, 0, y1, x2, u2, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, 0, y1, x1, u1, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, 0, y2, x1, u1, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, 0, y2, x2, u2, v2, nx, ny, nz, packedOverlay);
      }
      case EAST -> {
        vertex(vc, mat, 1, y1, x1, u2, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, 1, y1, x2, u1, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, 1, y2, x2, u1, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, 1, y2, x1, u2, v2, nx, ny, nz, packedOverlay);
      }
      case UP -> {
        vertex(vc, mat, x1, 1, y2, u1, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, 1, y2, u2, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, 1, y1, u2, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x1, 1, y1, u1, v1, nx, ny, nz, packedOverlay);
      }
      case DOWN -> {
        vertex(vc, mat, x1, 0, y1, u1, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, 0, y1, u2, v1, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x2, 0, y2, u2, v2, nx, ny, nz, packedOverlay);
        vertex(vc, mat, x1, 0, y2, u1, v2, nx, ny, nz, packedOverlay);
      }
    }
  }

  private void vertex(
      VertexConsumer vc,
      Matrix4f mat,
      float x, float y, float z,
      float u, float v,
      float nx, float ny, float nz,
      int packedOverlay
  ) {
    vc.addVertex(mat, x, y, z)
        .setColor(r, g, b, a)
        .setUv(u, v)
        .setOverlay(packedOverlay)
        .setLight(LightTexture.FULL_BRIGHT)
        .setNormal(nx, ny, nz)
    ;
  }
}