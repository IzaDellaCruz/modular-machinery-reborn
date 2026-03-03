package es.degrassi.mmreborn.common.integration.kubejs.events;

import com.google.common.collect.Lists;
import dev.latvian.mods.kubejs.event.KubeEvent;
import es.degrassi.mmreborn.api.client.machine.TooltipUse;
import es.degrassi.mmreborn.common.integration.kubejs.builder.ExtraTooltipsBuilderJS;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@Getter
public class ExtraTooltipsKubeEvent implements KubeEvent {
  private final List<ExtraTooltipsBuilderJS> builders = Lists.newArrayList();

  public ExtraTooltipsBuilderJS create(ResourceLocation id, TooltipUse useOn) {
    ExtraTooltipsBuilderJS builder = new ExtraTooltipsBuilderJS(id, useOn);
    builders.add(builder);
    return builder;
  }
}
