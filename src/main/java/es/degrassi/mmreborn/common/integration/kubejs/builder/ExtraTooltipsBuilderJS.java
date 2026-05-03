package es.degrassi.mmreborn.common.integration.kubejs.builder;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.machine.TooltipUse;
import lombok.AllArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AllArgsConstructor
public class ExtraTooltipsBuilderJS {
  private final ResourceLocation id;
  private final TooltipUse useOn;
  private final List<Either<ResourceLocation, Component>> components = Lists.newArrayList();

  public ExtraTooltipsBuilderJS add(Component extra) {
    components.add(Either.right(extra));
    return this;
  }

  public ExtraTooltipsBuilderJS addDynamic(String id) {
    try {
      components.add(Either.left(ResourceLocation.parse(id)));
    } catch(Exception ignored) {
      components.add(Either.left(ModularMachineryReborn.rl(id)));
    }
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
  public final List<Either<ResourceLocation, Component>> build() {
    return components;
  }
}
