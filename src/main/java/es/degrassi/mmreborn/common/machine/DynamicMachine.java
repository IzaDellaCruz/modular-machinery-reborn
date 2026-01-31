package es.degrassi.mmreborn.common.machine;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.api.codec.DefaultCodecs;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.codec.RegistrarCodec;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.entity.base.TextureableMachineEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.util.MachineModelLocation;
import es.degrassi.mmreborn.common.util.sound.AmbientSound;
import es.degrassi.mmreborn.common.util.sound.Sounds;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class DynamicMachine {
  public static final NamedCodec<DynamicMachine> CODEC = NamedCodec.record(instance -> instance.group(
      DefaultCodecs.RESOURCE_LOCATION.fieldOf("registryName").forGetter(DynamicMachine::getRegistryName),
      NamedCodec.STRING.optionalFieldOf("localizedName").forGetter(machine -> Optional.of(machine.getLocalizedName())),
      Structure.CODEC.fieldOf("structure").forGetter(DynamicMachine::getPattern),
      DefaultCodecs.HEX.optionalFieldOf("color", Config.machineColor).forGetter(DynamicMachine::getMachineColor),
      MachineModelLocation.CODEC.optionalFieldOf("controller", MachineModelLocation.DEFAULT).forGetter(DynamicMachine::getControllerModel),
      NamedCodec.unboundedMap(MachineStatus.CODEC, Sounds.CODEC, "Sounds by status").optionalFieldOf("sound", new HashMap<>()).forGetter(DynamicMachine::getSounds),
      NamedCodec.unboundedMap(
          RegistrarCodec.HATCH_TYPE,
          TextureableMachineEntity.CODEC,
          "Formed Textures by HatchType"
      ).optionalFieldOf("formed_textures", Maps.newHashMap()).forGetter(DynamicMachine::getFormedTextures)
  ).apply(instance,
      (registryName, localizedName, pattern, color, controllerModel, sounds, formedTextures) -> {
    DynamicMachine machine = new DynamicMachine(registryName, sounds, formedTextures);
    machine.setPattern(pattern);
    machine.setLocalizedName(localizedName);
    machine.setDefinedColor(color);
    machine.setControllerModel(controllerModel);
    return machine;
  }), "Dynamic Machine");

  public static final DynamicMachine DUMMY;
  static {
    Map<MachineStatus, Sounds> sounds = new HashMap<>();
    for (MachineStatus status : MachineStatus.values()) {
      sounds.put(status, Sounds.DEFAULT);
    }
    DUMMY = new DynamicMachine(ModularMachineryReborn.rl("dummy"), sounds, Maps.newHashMap());
  }

  @Nonnull
  private ResourceLocation registryName;
  private Optional<String> localizedName = Optional.empty();
  private Structure pattern = Structure.EMPTY;
  private int definedColor = Config.machineColor;
  private @Nullable MachineModelLocation controllerModel;
  private final Map<MachineStatus, Sounds> sounds;
  private final Map<MachineHatchType, Pair<Boolean, Pair<Optional<ResourceLocation>, Optional<ResourceLocation>>>> formedTextures;

  public DynamicMachine(ResourceLocation registryName, Map<MachineStatus, Sounds> sounds,
                        Map<MachineHatchType,
                            Pair<Boolean, Pair<Optional<ResourceLocation>, Optional<ResourceLocation>>>> formedTextures) {
    this.registryName = registryName;
    this.sounds = sounds;
    this.formedTextures = formedTextures;
  }

  public List<ModifierReplacement> getModifiers() {
    return getPattern().getPattern().getModifiers();
  }

  public String getLocalizedName() {
    return getName().getString();
  }

  public Component getName() {
    String localizationKey = registryName.getNamespace() + "." + registryName.getPath();
    return Component.translatableWithFallback(localizationKey, localizedName.orElse(localizationKey));
  }

  @Nullable
  public AmbientSound getAmbientSound(MachineStatus status) {
    return Optional.ofNullable(sounds.get(status)).map(Sounds::ambientSound).orElse(AmbientSound.DEFAULT);
  }

  public SoundType getInteractionSound(MachineStatus status) {
    return Optional.ofNullable(this.sounds.get(status)).orElse(Sounds.DEFAULT).interaction();
  }

  public int getMachineColor() {
    return definedColor;
  }

  public boolean isDummy() {
    return this.equals(DUMMY);
  }

  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    json.addProperty("registryName", registryName.toString());
    json.addProperty("localizedName", localizedName.orElse("null"));
    json.add("pattern", pattern.asJson());
    json.addProperty("definedColor", definedColor);
    if (controllerModel != null && controllerModel.getLoc() != null)
      json.addProperty("controllerModel", controllerModel.toString());
    JsonObject formedTexts = new JsonObject();
    formedTextures.forEach((hatchType, pair) -> {
      var shouldColor = pair.getFirst();
      var texts = pair.mapSecond(second -> {
        var baseTexture = second.getFirst();
        var overlayTexture = second.getSecond();
        JsonObject textures = new JsonObject();
        baseTexture.ifPresent(rl -> textures.addProperty("base_texture", rl.toString()));
        overlayTexture.ifPresent(rl -> textures.addProperty("overlay_texture", rl.toString()));
        return textures;
      }).getSecond();
      JsonObject withColor = new JsonObject();
      withColor.addProperty("should_color", shouldColor);
      withColor.add("textures", texts);
      formedTexts.add(hatchType.getId().toString(), withColor);
    });
    json.add("formed_textures", formedTexts);
    return json;
  }

  @Override
  public String toString() {
    return asJson().toString();
  }

  public List<List<ItemStack>> getStacks() {
    return getPattern()
        .getPattern()
        .asList()
        .stream()
        .flatMap(List::stream)
        .flatMap(s -> s.chars().mapToObj(c -> (char) c))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .map(entry -> {
          BlockIngredient ingredient = getPattern().getPattern().asMap().get(entry.getKey());
          if (ingredient == null) return null;
          return ingredient.getStacks(entry.getValue().intValue());
        })
        .filter(Objects::nonNull)
        .toList();
  }
}
