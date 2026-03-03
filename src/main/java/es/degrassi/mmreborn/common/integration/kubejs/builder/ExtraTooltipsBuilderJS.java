package es.degrassi.mmreborn.common.integration.kubejs.builder;

import com.google.common.collect.Lists;
import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.api.client.machine.TooltipUse;
import lombok.AllArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class ExtraTooltipsBuilderJS {
  private final ResourceLocation id;
  private final TooltipUse useOn;
  private final List<Component> components = Lists.newArrayList();

  public ExtraTooltipsBuilderJS add(Component...extras) {
    components.addAll(Arrays.asList(extras));
    return this;
  }

  public int size() {
    return components.size();
  }

  @HideFromJS
  public ResourceLocation getId() {
    return id;
  }

  @HideFromJS
  public TooltipUse getUseOn() {
    return useOn;
  }

  @HideFromJS
  public final List<Component> build() {
    return components;
  }
}
