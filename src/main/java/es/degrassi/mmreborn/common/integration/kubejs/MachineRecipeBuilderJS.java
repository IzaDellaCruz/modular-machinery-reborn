package es.degrassi.mmreborn.common.integration.kubejs;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.BiomeRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.ChunkloadRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.DimensionRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.DurabilityRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.EmptyRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.EnergyPerTickRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.EnergyRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.ExperienceRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.FluidRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.FunctionRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.HeightRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.ItemRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.LootTableRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.TimeRequirementJS;
import es.degrassi.mmreborn.common.integration.kubejs.requirement.WeatherRequirementJS;
import es.degrassi.mmreborn.common.registration.RecipeRegistration;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MachineRecipeBuilderJS extends KubeRecipe implements RecipeJSBuilder, EnergyPerTickRequirementJS, EnergyRequirementJS, ItemRequirementJS,
    FluidRequirementJS, DimensionRequirementJS, BiomeRequirementJS, WeatherRequirementJS, TimeRequirementJS, ChunkloadRequirementJS, LootTableRequirementJS,
    ExperienceRequirementJS, HeightRequirementJS, FunctionRequirementJS, DurabilityRequirementJS, EmptyRequirementJS
{

  @HideFromJS
  public static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = Maps.newHashMap();

  @HideFromJS
  private boolean jei = false;

  @HideFromJS
  public MachineRecipeBuilderJS(ResourceLocation machine, int time, int width, int height, int progressX, int progressY) {
    setValue(ModularMachineryRebornRecipeSchemas.MACHINE_ID, machine);
    setValue(ModularMachineryRebornRecipeSchemas.TIME, new TickDuration(time));
    setValue(ModularMachineryRebornRecipeSchemas.PROGRESS_X, progressX);
    setValue(ModularMachineryRebornRecipeSchemas.PROGRESS_Y, progressY);
    setValue(ModularMachineryRebornRecipeSchemas.WIDTH, width);
    setValue(ModularMachineryRebornRecipeSchemas.HEIGHT, height);
    setValue(ModularMachineryRebornRecipeSchemas.SHOULD_RENDER_PROGRESS, true);
  }
  @HideFromJS
  public MachineRecipeBuilderJS(ResourceLocation machine, int time, int width, int height) {
    setValue(ModularMachineryRebornRecipeSchemas.MACHINE_ID, machine);
    setValue(ModularMachineryRebornRecipeSchemas.TIME, new TickDuration(time));
    setValue(ModularMachineryRebornRecipeSchemas.WIDTH, width);
    setValue(ModularMachineryRebornRecipeSchemas.HEIGHT, height);
    setValue(ModularMachineryRebornRecipeSchemas.SHOULD_RENDER_PROGRESS, true);
  }

  @HideFromJS
  public MachineRecipeBuilderJS(ResourceLocation machine, int time) {
    setValue(ModularMachineryRebornRecipeSchemas.MACHINE_ID, machine);
    setValue(ModularMachineryRebornRecipeSchemas.TIME, new TickDuration(time));
    setValue(ModularMachineryRebornRecipeSchemas.PROGRESS_X, 74);
    setValue(ModularMachineryRebornRecipeSchemas.PROGRESS_Y, 8);
    setValue(ModularMachineryRebornRecipeSchemas.WIDTH, 256);
    setValue(ModularMachineryRebornRecipeSchemas.HEIGHT, 256);
    setValue(ModularMachineryRebornRecipeSchemas.SHOULD_RENDER_PROGRESS, true);
  }

  @HideFromJS
  public MachineRecipeBuilderJS() {}

  @Override
  @HideFromJS
  public void afterLoaded() {
    super.afterLoaded();
    ResourceLocation machine = getValue(ModularMachineryRebornRecipeSchemas.MACHINE_ID);
    if(machine == null)
      throw new KubeRuntimeException("Invalid machine id: " + getValue(ModularMachineryRebornRecipeSchemas.MACHINE_ID));

    if (this.newRecipe) {
      int uniqueID = IDS.computeIfAbsent(RecipeRegistration.RECIPE_TYPE.getId(), id -> new HashMap<>()).computeIfAbsent(machine, m -> 0);
      IDS.get(RecipeRegistration.RECIPE_TYPE.getId()).put(machine, uniqueID + 1);
      this.id = ResourceLocation.fromNamespaceAndPath("kubejs", RecipeRegistration.RECIPE_TYPE.getId().getPath() + "/" + machine.getNamespace() + "/" + machine.getPath() + "/" + uniqueID);
    }
  }

  @Override
  @HideFromJS
  public @Nullable KubeRecipe serializeChanges() {
    if(!this.newRecipe)
      return super.serializeChanges();

    MachineRecipe.MachineRecipeBuilder builder = new MachineRecipe.MachineRecipeBuilder(
        getValue(ModularMachineryRebornRecipeSchemas.MACHINE_ID),
        (int) getValue(ModularMachineryRebornRecipeSchemas.TIME).ticks(),
        getValue(ModularMachineryRebornRecipeSchemas.WIDTH),
        getValue(ModularMachineryRebornRecipeSchemas.HEIGHT),
        new PositionedRequirement(
            getValue(ModularMachineryRebornRecipeSchemas.PROGRESS_X),
            getValue(ModularMachineryRebornRecipeSchemas.PROGRESS_Y)
        )
    );

    for (RecipeRequirement<?, ?> requirement : getValue(ModularMachineryRebornRecipeSchemas.REQUIREMENTS))
      builder.addRequirement(requirement);

    builder.addJeiRequirements(getValue(ModularMachineryRebornRecipeSchemas.JEI_REQUIREMENTS));

    if (getValue(ModularMachineryRebornRecipeSchemas.HIDE))
      builder.hide();

    builder.shouldRenderProgress(getValue(ModularMachineryRebornRecipeSchemas.SHOULD_RENDER_PROGRESS));
    builder.withPriority(getValue(ModularMachineryRebornRecipeSchemas.PRIORITY));
    builder.shouldVoidOnFailure(getValue(ModularMachineryRebornRecipeSchemas.VOID));

    this.id = getOrCreateId();
    DataResult<JsonElement> result =
        MachineRecipe.CODEC.encodeStart(this.type.event.registries.json(), builder);
    if(result.result().isPresent())
      this.json = (JsonObject) result.result().get();
    else if(result.error().isPresent()) {
      ConsoleJS.SERVER.error("Error in Modular Machinery recipe: " + this.id + "\n" + result.error().get().message());
      this.json = new JsonObject();
    }
    if (this.json != null)
      this.json.addProperty("type", RecipeRegistration.RECIPE_TYPE.getId().toString());
    return this;
  }

  public MachineRecipeBuilderJS jei() {
    this.jei = true;
    return this;
  }

  public MachineRecipeBuilderJS hide() {
    setValue(ModularMachineryRebornRecipeSchemas.HIDE, true);
    return this;
  }

  public MachineRecipeBuilderJS renderProgress(boolean value) {
    setValue(ModularMachineryRebornRecipeSchemas.SHOULD_RENDER_PROGRESS, value);
    return this;
  }

  public MachineRecipeBuilderJS progressX(int x) {
    setValue(ModularMachineryRebornRecipeSchemas.PROGRESS_X, x);
    return this;
  }

  public MachineRecipeBuilderJS progressY(int y) {
    setValue(ModularMachineryRebornRecipeSchemas.PROGRESS_Y, y);
    return this;
  }

  public MachineRecipeBuilderJS width(int width) {
    setValue(ModularMachineryRebornRecipeSchemas.WIDTH, width);
    return this;
  }

  public MachineRecipeBuilderJS height(int height) {
    setValue(ModularMachineryRebornRecipeSchemas.HEIGHT, height);
    return this;
  }

  public MachineRecipeBuilderJS voidOnFailure(boolean v) {
    setValue(ModularMachineryRebornRecipeSchemas.VOID, v);
    return this;
  }

  public MachineRecipeBuilderJS priority(int priority) {
    setValue(ModularMachineryRebornRecipeSchemas.PRIORITY, priority);
    return this;
  }

  @Override
  @HideFromJS
  public MachineRecipeBuilderJS addRequirement(RecipeRequirement<?, ?> requirement) {
    if(!this.jei)
      setValue(ModularMachineryRebornRecipeSchemas.REQUIREMENTS, addToList(ModularMachineryRebornRecipeSchemas.REQUIREMENTS, requirement));
    else
      setValue(ModularMachineryRebornRecipeSchemas.JEI_REQUIREMENTS, addToList(ModularMachineryRebornRecipeSchemas.JEI_REQUIREMENTS, requirement));
    return this;
  }

  @HideFromJS
  private <E> List<E> addToList(RecipeKey<List<E>> key, E element) {
    List<E> list = new ArrayList<>(getValue(key));
    list.add(element);
    return list;
  }

  @Override
  @HideFromJS
  public MachineRecipeBuilderJS error(String error, Object... args) {
    throw new KubeRuntimeException(MessageFormatter.arrayFormat(error, args).getMessage()).source(this.sourceLine);
  }
}
