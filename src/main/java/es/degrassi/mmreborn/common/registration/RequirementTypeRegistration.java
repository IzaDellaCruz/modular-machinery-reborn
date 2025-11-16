package es.degrassi.mmreborn.common.registration;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementBiome;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementChunkload;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementCommand;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDimension;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDurability;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDurabilityPerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDuration;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEffect;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEmpty;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEnergy;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEnergyPerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperience;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperiencePerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluid;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluidPerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFuel;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFunction;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementLootTable;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementRedstone;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementStructure;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementTime;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementHeight;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementWeather;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementCheckEntity;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementHealthEntity;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementKillEntity;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementSpawnEntity;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class RequirementTypeRegistration {
  private RequirementTypeRegistration() {}
  public static final DeferredRegister<RequirementType<? extends IRequirement<?>>> MACHINE_REQUIREMENTS =
      DeferredRegister.create(RequirementType.REGISTRY_KEY, ModularMachineryReborn.MODID);

  public static final Registry<RequirementType<? extends IRequirement<?>>> REQUIREMENTS_REGISTRY =
      MACHINE_REQUIREMENTS.makeRegistry(builder -> {});

  public static final Supplier<RequirementType<RequirementItem>> ITEM =
      MACHINE_REQUIREMENTS.register(rootLC("item"),
      () -> RequirementType.inventory(RequirementItem.CODEC));
  public static final Supplier<RequirementType<RequirementEmpty>> EMPTY =
      MACHINE_REQUIREMENTS.register(rootLC("empty"),
      () -> RequirementType.inventory(RequirementEmpty.CODEC));
  public static final Supplier<RequirementType<RequirementDurability>> DURABILITY =
      MACHINE_REQUIREMENTS.register(rootLC("durability"),
      () -> RequirementType.inventory(RequirementDurability.CODEC));
  public static final Supplier<RequirementType<RequirementDurabilityPerTick>> DURABILITY_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("durability_per_tick"),
          () -> RequirementType.inventory(RequirementDurabilityPerTick.CODEC));
  public static final Supplier<RequirementType<RequirementFluid>> FLUID =
      MACHINE_REQUIREMENTS.register(rootLC("fluid"),
      () -> RequirementType.inventory(RequirementFluid.CODEC));
  public static final Supplier<RequirementType<RequirementFluidPerTick>> FLUID_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("fluid_per_tick"),
          () -> RequirementType.inventory(RequirementFluidPerTick.CODEC));
  public static final Supplier<RequirementType<RequirementEnergyPerTick>> ENERGY_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("energy_per_tick"),
      () -> RequirementType.inventory(RequirementEnergyPerTick.CODEC));
  public static final Supplier<RequirementType<RequirementEnergy>> ENERGY =
      MACHINE_REQUIREMENTS.register(rootLC("energy"),
          () -> RequirementType.inventory(RequirementEnergy.CODEC));
  public static final Supplier<RequirementType<RequirementDuration>> SPEED =
      MACHINE_REQUIREMENTS.register(rootLC("speed"),
      () -> RequirementType.inventory(RequirementDuration.CODEC));
  public static final Supplier<RequirementType<RequirementDimension>> DIMENSION =
      MACHINE_REQUIREMENTS.register(rootLC("dimension"),
      () -> RequirementType.world(RequirementDimension.CODEC));
  public static final Supplier<RequirementType<RequirementBiome>> BIOME =
      MACHINE_REQUIREMENTS.register(rootLC("biome"),
      () -> RequirementType.world(RequirementBiome.CODEC));
  public static final Supplier<RequirementType<RequirementWeather>> WEATHER =
      MACHINE_REQUIREMENTS.register(rootLC("weather"),
      () -> RequirementType.world(RequirementWeather.CODEC));
  public static final Supplier<RequirementType<RequirementTime>> TIME =
      MACHINE_REQUIREMENTS.register(rootLC("time"),
      () -> RequirementType.world(RequirementTime.CODEC));
  public static final Supplier<RequirementType<RequirementHeight>> HEIGHT =
      MACHINE_REQUIREMENTS.register(rootLC("height"),
      () -> RequirementType.world(RequirementHeight.CODEC));
  public static final Supplier<RequirementType<RequirementChunkload>> CHUNKLOAD =
      MACHINE_REQUIREMENTS.register(rootLC("chunkload"),
      () -> RequirementType.world(RequirementChunkload.CODEC));
  public static final Supplier<RequirementType<RequirementLootTable>> LOOT_TABLE =
      MACHINE_REQUIREMENTS.register(rootLC("loot_table"),
      () -> RequirementType.inventory(RequirementLootTable.CODEC));
  public static final Supplier<RequirementType<RequirementExperience>> EXPERIENCE =
      MACHINE_REQUIREMENTS.register(rootLC("experience"),
      () -> RequirementType.inventory(RequirementExperience.CODEC));
  public static final Supplier<RequirementType<RequirementExperiencePerTick>> EXPERIENCE_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("experience_per_tick"),
          () -> RequirementType.inventory(RequirementExperiencePerTick.CODEC));
  public static final Supplier<RequirementType<RequirementFunction>> FUNCTION =
      MACHINE_REQUIREMENTS.register(rootLC("function"),
      () -> RequirementType.world(RequirementFunction.CODEC));
  public static final Supplier<RequirementType<RequirementFuel>> FUEL =
      MACHINE_REQUIREMENTS.register(rootLC("fuel"),
      () -> RequirementType.inventory(RequirementFuel.CODEC));
  public static final Supplier<RequirementType<RequirementEffect>> EFFECT =
      MACHINE_REQUIREMENTS.register(rootLC("effect"),
      () -> RequirementType.world(RequirementEffect.CODEC));
  public static final Supplier<RequirementType<RequirementKillEntity>> KILL_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("kill_entity"),
      () -> RequirementType.world(RequirementKillEntity.CODEC));
  public static final Supplier<RequirementType<RequirementCheckEntity>> CHECK_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("check_entity"),
      () -> RequirementType.world(RequirementCheckEntity.CODEC));
  public static final Supplier<RequirementType<RequirementSpawnEntity>> SPAWN_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("spawn_entity"),
      () -> RequirementType.world(RequirementSpawnEntity.CODEC));
  public static final Supplier<RequirementType<RequirementHealthEntity>> HEATH_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("health_entity"),
      () -> RequirementType.world(RequirementHealthEntity.CODEC));
  public static final Supplier<RequirementType<RequirementStructure>> STRUCTURE =
      MACHINE_REQUIREMENTS.register(rootLC("structure"),
      () -> RequirementType.world(RequirementStructure.CODEC));
  public static final Supplier<RequirementType<RequirementRedstone>> REDSTONE =
      MACHINE_REQUIREMENTS.register(rootLC("redstone"),
      () -> RequirementType.world(RequirementRedstone.CODEC));
  public static final Supplier<RequirementType<RequirementCommand>> COMMAND =
      MACHINE_REQUIREMENTS.register(rootLC("command"),
      () -> RequirementType.world(RequirementCommand.CODEC));

  public static void register(IEventBus bus) {
    MACHINE_REQUIREMENTS.register(bus);
  }
}
