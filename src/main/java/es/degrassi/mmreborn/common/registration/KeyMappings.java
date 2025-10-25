package es.degrassi.mmreborn.common.registration;

import com.mojang.blaze3d.platform.InputConstants;
import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

@EventBusSubscriber(modid = ModularMachineryReborn.MODID, value = Dist.CLIENT)
public abstract class KeyMappings {

  private KeyMappings() {}

  public static final Lazy<KeyMapping> STRUCTURE_MODE_CHANGE = Lazy.of(() -> new KeyMapping(
      "key." + ModularMachineryReborn.MODID + ".STRUCTURE_MODE_CHANGE".toLowerCase(Locale.ROOT),
      KeyConflictContext.IN_GAME,
      InputConstants.Type.KEYSYM,
      GLFW.GLFW_KEY_MINUS,
      "key.categories" + ModularMachineryReborn.MODID
  ));

  @SubscribeEvent
  public static void registerBindings(RegisterKeyMappingsEvent event) {
    event.register(STRUCTURE_MODE_CHANGE.get());
  }
}
