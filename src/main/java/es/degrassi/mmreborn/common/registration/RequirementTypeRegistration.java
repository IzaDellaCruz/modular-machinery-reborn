package es.degrassi.mmreborn.common.registration;

import es.degrassi.experiencelib.api.capability.IExperienceHandler;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.api.capability.EffectHandler;
import es.degrassi.mmreborn.api.capability.EntityHandler;
import es.degrassi.mmreborn.api.capability.IFuelHandler;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.WeatherType;
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
import es.degrassi.mmreborn.common.machine.component.BiomeComponent;
import es.degrassi.mmreborn.common.machine.component.ChunkloadComponent;
import es.degrassi.mmreborn.common.machine.component.CommandComponent;
import es.degrassi.mmreborn.common.machine.component.DimensionComponent;
import es.degrassi.mmreborn.common.machine.component.DurabilityComponent;
import es.degrassi.mmreborn.common.machine.component.DurationComponent;
import es.degrassi.mmreborn.common.machine.component.EffectComponent;
import es.degrassi.mmreborn.common.machine.component.EmptyComponent;
import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.machine.component.EntityComponent;
import es.degrassi.mmreborn.common.machine.component.ExperienceComponent;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.machine.component.FuelComponent;
import es.degrassi.mmreborn.common.machine.component.FunctionComponent;
import es.degrassi.mmreborn.common.machine.component.HeightComponent;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.machine.component.RedstoneComponent;
import es.degrassi.mmreborn.common.machine.component.StructureComponent;
import es.degrassi.mmreborn.common.machine.component.TimeComponent;
import es.degrassi.mmreborn.common.machine.component.WeatherComponent;
import es.degrassi.mmreborn.common.util.Chunkloader;
import es.degrassi.mmreborn.common.util.HybridTank;
import es.degrassi.mmreborn.common.util.IEnergyHandler;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.IntRange;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class RequirementTypeRegistration {
  private RequirementTypeRegistration() {}
  public static final DeferredRegister<RequirementType<? extends IRequirement<?, ?>, ?, ?>> MACHINE_REQUIREMENTS =
      DeferredRegister.create(RequirementType.REGISTRY_KEY, ModularMachineryReborn.MODID);

  public static final Registry<RequirementType<? extends IRequirement<?, ?>, ?, ?>> REQUIREMENTS_REGISTRY =
      MACHINE_REQUIREMENTS.makeRegistry(builder -> {});

  public static final Supplier<RequirementType<RequirementItem, ItemComponent, IOInventory>> ITEM =
      MACHINE_REQUIREMENTS.register(rootLC("item"),
      () -> RequirementType.inventory(RequirementItem.CODEC));
  public static final Supplier<RequirementType<RequirementEmpty, EmptyComponent, Void>> EMPTY =
      MACHINE_REQUIREMENTS.register(rootLC("empty"),
      () -> RequirementType.inventory(RequirementEmpty.CODEC));
  public static final Supplier<RequirementType<RequirementDurability, DurabilityComponent, IOInventory>> DURABILITY =
      MACHINE_REQUIREMENTS.register(rootLC("durability"),
      () -> RequirementType.inventory(RequirementDurability.CODEC));
  public static final Supplier<RequirementType<RequirementDurabilityPerTick, DurabilityComponent, IOInventory>> DURABILITY_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("durability_per_tick"),
      () -> RequirementType.inventory(RequirementDurabilityPerTick.CODEC));
  public static final Supplier<RequirementType<RequirementFluid, FluidComponent, HybridTank>> FLUID =
      MACHINE_REQUIREMENTS.register(rootLC("fluid"),
      () -> RequirementType.inventory(RequirementFluid.CODEC));
  public static final Supplier<RequirementType<RequirementFluidPerTick, FluidComponent, HybridTank>> FLUID_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("fluid_per_tick"),
      () -> RequirementType.inventory(RequirementFluidPerTick.CODEC));
  public static final Supplier<RequirementType<RequirementEnergyPerTick, EnergyComponent, IEnergyHandler>> ENERGY_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("energy_per_tick"),
      () -> RequirementType.inventory(RequirementEnergyPerTick.CODEC));
  public static final Supplier<RequirementType<RequirementEnergy, EnergyComponent, IEnergyHandler>> ENERGY =
      MACHINE_REQUIREMENTS.register(rootLC("energy"),
      () -> RequirementType.inventory(RequirementEnergy.CODEC));
  public static final Supplier<RequirementType<RequirementDuration, DurationComponent, Void>> SPEED =
      MACHINE_REQUIREMENTS.register(rootLC("speed"),
      () -> RequirementType.inventory(RequirementDuration.CODEC));
  public static final Supplier<RequirementType<RequirementDimension, DimensionComponent, ResourceLocation>> DIMENSION =
      MACHINE_REQUIREMENTS.register(rootLC("dimension"),
      () -> RequirementType.world(RequirementDimension.CODEC));
  public static final Supplier<RequirementType<RequirementBiome, BiomeComponent, List<ResourceLocation>>> BIOME =
      MACHINE_REQUIREMENTS.register(rootLC("biome"),
      () -> RequirementType.world(RequirementBiome.CODEC));
  public static final Supplier<RequirementType<RequirementWeather, WeatherComponent, WeatherType>> WEATHER =
      MACHINE_REQUIREMENTS.register(rootLC("weather"),
      () -> RequirementType.world(RequirementWeather.CODEC));
  public static final Supplier<RequirementType<RequirementTime, TimeComponent, IntRange>> TIME =
      MACHINE_REQUIREMENTS.register(rootLC("time"),
      () -> RequirementType.world(RequirementTime.CODEC));
  public static final Supplier<RequirementType<RequirementHeight, HeightComponent, IntRange>> HEIGHT =
      MACHINE_REQUIREMENTS.register(rootLC("height"),
      () -> RequirementType.world(RequirementHeight.CODEC));
  public static final Supplier<RequirementType<RequirementChunkload, ChunkloadComponent, Chunkloader>> CHUNKLOAD =
      MACHINE_REQUIREMENTS.register(rootLC("chunkload"),
      () -> RequirementType.world(RequirementChunkload.CODEC));
  public static final Supplier<RequirementType<RequirementLootTable, ItemComponent, IOInventory>> LOOT_TABLE =
      MACHINE_REQUIREMENTS.register(rootLC("loot_table"),
      () -> RequirementType.inventory(RequirementLootTable.CODEC));
  public static final Supplier<RequirementType<RequirementExperience, ExperienceComponent, IExperienceHandler>> EXPERIENCE =
      MACHINE_REQUIREMENTS.register(rootLC("experience"),
      () -> RequirementType.inventory(RequirementExperience.CODEC));
  public static final Supplier<RequirementType<RequirementExperiencePerTick, ExperienceComponent, IExperienceHandler>> EXPERIENCE_PER_TICK =
      MACHINE_REQUIREMENTS.register(rootLC("experience_per_tick"),
      () -> RequirementType.inventory(RequirementExperiencePerTick.CODEC));
  public static final Supplier<RequirementType<RequirementFunction, FunctionComponent, Void>> FUNCTION =
      MACHINE_REQUIREMENTS.register(rootLC("function"),
      () -> RequirementType.world(RequirementFunction.CODEC));
  public static final Supplier<RequirementType<RequirementFuel, FuelComponent, IFuelHandler>> FUEL =
      MACHINE_REQUIREMENTS.register(rootLC("fuel"),
      () -> RequirementType.inventory(RequirementFuel.CODEC));
  public static final Supplier<RequirementType<RequirementEffect, EffectComponent, EffectHandler>> EFFECT =
      MACHINE_REQUIREMENTS.register(rootLC("effect"),
      () -> RequirementType.world(RequirementEffect.CODEC));
  public static final Supplier<RequirementType<RequirementKillEntity, EntityComponent, EntityHandler>> KILL_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("kill_entity"),
      () -> RequirementType.world(RequirementKillEntity.CODEC));
  public static final Supplier<RequirementType<RequirementCheckEntity, EntityComponent, EntityHandler>> CHECK_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("check_entity"),
      () -> RequirementType.world(RequirementCheckEntity.CODEC));
  public static final Supplier<RequirementType<RequirementSpawnEntity, EntityComponent, EntityHandler>> SPAWN_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("spawn_entity"),
      () -> RequirementType.world(RequirementSpawnEntity.CODEC));
  public static final Supplier<RequirementType<RequirementHealthEntity, EntityComponent, EntityHandler>> HEATH_ENTITY =
      MACHINE_REQUIREMENTS.register(rootLC("health_entity"),
      () -> RequirementType.world(RequirementHealthEntity.CODEC));
  public static final Supplier<RequirementType<RequirementStructure, StructureComponent, Structure>> STRUCTURE =
      MACHINE_REQUIREMENTS.register(rootLC("structure"),
      () -> RequirementType.world(RequirementStructure.CODEC));
  public static final Supplier<RequirementType<RequirementRedstone, RedstoneComponent, Integer>> REDSTONE =
      MACHINE_REQUIREMENTS.register(rootLC("redstone"),
      () -> RequirementType.world(RequirementRedstone.CODEC));
  public static final Supplier<RequirementType<RequirementCommand, CommandComponent, Void>> COMMAND =
      MACHINE_REQUIREMENTS.register(rootLC("command"),
      () -> RequirementType.world(RequirementCommand.CODEC));

  public static void register(IEventBus bus) {
    MACHINE_REQUIREMENTS.register(bus);
  }
}
