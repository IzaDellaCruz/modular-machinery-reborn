package es.degrassi.mmreborn.common.util;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * A helper class to find the size of a texture, do not call its methods on a dedicated server as it will immediately
 * crash.
 */
public class TextureSizeHelper {
  private TextureSizeHelper() {}

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Map<ResourceLocation, Pair<Integer, Integer>> SIZES = Maps.newHashMap();

  public static int getWidth(@Nullable ResourceLocation texture) {
    if (texture == null)
      return 0;
    else if (SIZES.containsKey(texture))
      return SIZES.get(texture).getLeft();
    else {
      try {
        BufferedImage image = ImageIO.read(Minecraft.getInstance().getResourceManager().open(texture));
        int width = image.getWidth();
        SIZES.put(texture, Pair.of(width, image.getHeight()));
        return width;
      } catch (IOException e) {
        log(texture);
      }
      return 0;
    }
  }

  public static int getHeight(@Nullable ResourceLocation texture) {
    if (texture == null)
      return 0;
    else if (SIZES.containsKey(texture))
      return SIZES.get(texture).getRight();
    else {
      try {
        BufferedImage image = ImageIO.read(Minecraft.getInstance().getResourceManager().open(texture));
        SIZES.put(texture, Pair.of(image.getWidth(), image.getHeight()));
        return image.getHeight();
      } catch (IOException e) {
        log(texture);
      }
      return 0;
    }
  }

  private static void log(ResourceLocation texture) {
    LOGGER.warn("No texture found for location: {}", texture);
  }
}
