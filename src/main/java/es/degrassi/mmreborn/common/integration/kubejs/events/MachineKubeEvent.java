package es.degrassi.mmreborn.common.integration.kubejs.events;


import com.google.common.collect.Lists;
import dev.latvian.mods.kubejs.event.KubeEvent;
import es.degrassi.mmreborn.common.integration.kubejs.builder.MachineBuilderJS;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@Getter
public class MachineKubeEvent implements KubeEvent {
  private final List<MachineBuilderJS> builders = Lists.newArrayList();

  public MachineBuilderJS create(ResourceLocation id) {
    MachineBuilderJS builder = new MachineBuilderJS(id);
    builders.add(builder);
    return builder;
  }
}