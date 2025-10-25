package es.degrassi.mmreborn.common.integration.kubejs.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.event.KubeEvent;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.api.codec.DefaultCodecs;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.util.sound.Sounds;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.util.MachineModelLocation;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MachineBuilderJS {
  @NotNull
  @Getter
  private final ResourceLocation id;
  private String name;
  private String color;
  private Integer intColor;
  private StructureBuilderJS structure;
  private MachineModelLocation controllerModel;
  private final List<ModifierReplacement> modifiers;
  private final Map<MachineStatus, Sounds> sounds;
  private final Map<MachineHatchType, Pair<Boolean, Pair<Optional<ResourceLocation>, Optional<ResourceLocation>>>> textureMap;

  public MachineBuilderJS(@NotNull ResourceLocation id) {
    this.id = id;
    modifiers = Lists.newArrayList();
    sounds = Maps.newEnumMap(MachineStatus.class);
    textureMap = Maps.newHashMap();
  }

  public MachineBuilderJS name(String name) {
    this.name = name;
    return this;
  }

  public MachineBuilderJS color(String color) {
    this.color = color;
    return this;
  }

  public MachineBuilderJS color(Integer color) {
    this.intColor = color;
    return this;
  }

  public MachineBuilderJS structure(StructureBuilderJS structure) {
    this.structure = structure;
    return this;
  }

  public MachineBuilderJS controllerModel(MachineModelLocation modelLocation) {
    this.controllerModel = modelLocation;
    return this;
  }

  public MachineBuilderJS texture(ResourceLocation type, boolean shouldColor, @Nullable ResourceLocation baseTexture,
                                  @Nullable ResourceLocation overlayTexture) {
    Objects.requireNonNull(type, "HatchType cant be null");
    MachineHatchType t = ModularMachineryReborn.getMachineHatchTypeRegistrar().get(type);
    Objects.requireNonNull(t, String.format("Invalid HatchType, given: %s", type));
    var base = Optional.ofNullable(baseTexture);
    var overlay = Optional.ofNullable(overlayTexture);
    var pair = Pair.of(base, overlay);
    textureMap.put(t, Pair.of(shouldColor, pair));
    return this;
  }

  public MachineBuilderJS addModifier(ModifierBuilderJS modifier) {
    this.modifiers.add(modifier.build());
    return this;
  }

  public MachineBuilderJS sound(MachineStatus status, Sounds sounds) {
    this.sounds.put(status, sounds);
    return this;
  }

  public DynamicMachine build() {
    DynamicMachine machine = new DynamicMachine(id, sounds, textureMap);
    machine.setPattern(structure == null ? Structure.EMPTY : structure.build(modifiers));
    machine.setControllerModel(Objects.requireNonNullElse(controllerModel, MachineModelLocation.DEFAULT));
    machine.setLocalizedName(Optional.ofNullable(name));
    if (intColor != null)
      machine.setDefinedColor(intColor);
    else if(color != null)
      machine.setDefinedColor(DefaultCodecs.HEX.decode(JsonOps.INSTANCE, new JsonPrimitive(color)).result().orElse(new Pair<>(Config.toInt(MMRConfig.get().general_casing_color.get(), 0xFF4900), null)).getFirst());
    return machine;
  }

  @Getter
  public static class MachineKubeEvent implements KubeEvent {
    private final List<MachineBuilderJS> builders = Lists.newArrayList();

    public MachineBuilderJS create(ResourceLocation id) {
      MachineBuilderJS builder = new MachineBuilderJS(id);
      builders.add(builder);
      return builder;
    }
  }
}
