package es.degrassi.mmreborn;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import es.degrassi.experiencelib.api.capability.ExperienceLibCapabilities;
import es.degrassi.mmreborn.api.crafting.IProcessor;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.network.DataType;
import es.degrassi.mmreborn.api.network.IData;
import es.degrassi.mmreborn.client.util.EnergyDisplayUtil;
import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.block.prop.ConfigLoaded;
import es.degrassi.mmreborn.common.block.prop.EnergyHatchSize;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.block.prop.FluidHatchSize;
import es.degrassi.mmreborn.common.block.prop.ItemBusSize;
import es.degrassi.mmreborn.common.block.prop.ItemDurabilityHatchSize;
import es.degrassi.mmreborn.common.block.prop.ParallelHatchSize;
import es.degrassi.mmreborn.common.command.MMRCommand;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.util.EmptyRequirementType;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.integration.theoneprobe.TOPInfoProvider;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.machine.MachineHatchType;
import es.degrassi.mmreborn.common.machine.MachineJsonReloadListener;
import es.degrassi.mmreborn.common.manager.crafting.ProcessorType;
import es.degrassi.mmreborn.common.network.server.SLootTablesPacket;
import es.degrassi.mmreborn.common.network.server.SSyncMachinesPacket;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.registration.DataRegistration;
import es.degrassi.mmreborn.common.registration.EmptyRequirementTypeRegistration;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.MachineHatchTypeRegistration;
import es.degrassi.mmreborn.common.registration.ProcessorTypeRegistration;
import es.degrassi.mmreborn.common.registration.Registration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import es.degrassi.mmreborn.common.util.LootTableHelper;
import es.degrassi.mmreborn.common.util.MMRLogger;
import es.degrassi.mmreborn.common.util.MiscUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Mod(ModularMachineryReborn.MODID)
public class ModularMachineryReborn {
  public static final String MODID = "modular_machinery_reborn";
  public static final Logger LOGGER = LogManager.getLogger("Modular Machinery Reborn");

  public static final BiMap<ResourceLocation, DynamicMachine> MACHINES = HashBiMap.create();
  public static final BiMap<ResourceLocation, BlockController> MACHINES_BLOCK = HashBiMap.create();

  public ModularMachineryReborn(final ModContainer CONTAINER, final IEventBus MOD_BUS) {
    CONTAINER.registerConfig(ModConfig.Type.COMMON, MMRConfig.getSpec(), String.format("%s/base/common.toml", MODID));

    addConfigLoaders();

    Registration.register(MOD_BUS);

    MOD_BUS.addListener(this::commonSetup);
    MOD_BUS.addListener(this::sendIMCMessages);

    MOD_BUS.addListener(this::registerCapabilities);
    MOD_BUS.addListener(this::reloadConfig);

    final IEventBus GAME_BUS = NeoForge.EVENT_BUS;
    GAME_BUS.addListener(this::serverStarting);
    GAME_BUS.addListener(this::syncDatapacks);
    GAME_BUS.addListener(this::registerReloadListener);
    GAME_BUS.addListener(this::registerCommands);
    GAME_BUS.addListener(this::onReloadStart);
  }

  private static void addConfigLoaders() {
    ConfigLoaded.add(
        Pair.of(EnergyHatchSize.class, (EnergyHatchSize size) -> {
          size.maxEnergy = MMRConfig.get().energySize(size);
          size.maxEnergy = MiscUtils.clamp(size.maxEnergy, 1, Long.MAX_VALUE);
          size.transferLimit = MMRConfig.get().energyLimit(size);
          size.transferLimit = MiscUtils.clamp(size.transferLimit, 1, Long.MAX_VALUE);
        }),
        Pair.of(FluidHatchSize.class, (FluidHatchSize size) -> size.size = MMRConfig.get().fluidSize(size)),
        Pair.of(ItemBusSize.class, (ItemBusSize size) -> {
          size.slots = MMRConfig.get().itemSize(size);
          size.cols = MMRConfig.get().itemCols(size);
        }),
        Pair.of(ItemDurabilityHatchSize.class, (ItemDurabilityHatchSize size) -> {
          size.slots = MMRConfig.get().durabilitySize(size);
          size.cols = MMRConfig.get().durabilityCols(size);
        }),
        Pair.of(ExperienceHatchSize.class, (ExperienceHatchSize size) -> size.capacity =
            MMRConfig.get().experienceSize(size)),
        Pair.of(ParallelHatchSize.class, (ParallelHatchSize size) -> size.max = MMRConfig.get().maxParallel(size))
    );
  }

  private void sendIMCMessages(final InterModEnqueueEvent event) {
    if (ModList.get().isLoaded("theoneprobe"))
      InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPInfoProvider::new);
  }

  private void serverStarting(final ServerStartingEvent event) {
    LootTableHelper.generate(event.getServer());
  }

  private void syncDatapacks(final OnDatapackSyncEvent event) {
    if (event.getPlayer() != null)
      syncData(event.getPlayer());
    else {
      LootTableHelper.generate(event.getPlayerList().getServer());
      event.getPlayerList().getPlayers().forEach(this::syncData);
    }
  }

  public void syncData(ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, new SSyncMachinesPacket(MACHINES));
    PacketDistributor.sendToPlayer(player, new SLootTablesPacket(LootTableHelper.getLoots()));
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    MMRLogger.init();

    Config.load();
    ConfigLoaded.load();
    EnergyDisplayUtil.loadFromConfig();
  }

  private void reloadConfig(final ModConfigEvent.Reloading event) {
    if (event.getConfig().getSpec() == MMRConfig.getSpec()) {
      MMRLogger.setDebugLevel(MMRConfig.get().debugLevel.get().getLevel());
      Config.load();
      ConfigLoaded.load();
      EnergyDisplayUtil.loadFromConfig();
    }
  }

  private void registerCapabilities(final RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        EntityRegistration.ITEM_INPUT_BUS.get(),
        (be, side) -> be.getInventory()
    );
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        EntityRegistration.ITEM_OUTPUT_BUS.get(),
        (be, side) -> be.getInventory()
    );
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        EntityRegistration.ITEM_DURABILITY_HATCH.get(),
        (be, side) -> be.getInventory()
    );
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        EntityRegistration.FLUID_INPUT_HATCH.get(),
        (be, side) -> be.getTank()
    );
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        EntityRegistration.FLUID_OUTPUT_HATCH.get(),
        (be, side) -> be.getTank()
    );
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        EntityRegistration.ENERGY_INPUT_HATCH.get(),
        (be, side) -> be
    );
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK,
        EntityRegistration.ENERGY_OUTPUT_HATCH.get(),
        (be, side) -> be
    );
    event.registerBlockEntity(
        ExperienceLibCapabilities.EXPERIENCE.block(),
        EntityRegistration.EXPERIENCE_INPUT_HATCH.get(),
        (be, side) -> be.getTank()
    );
    event.registerBlockEntity(
        ExperienceLibCapabilities.EXPERIENCE.block(),
        EntityRegistration.EXPERIENCE_OUTPUT_HATCH.get(),
        (be, side) -> be.getTank()
    );
  }

  private void registerReloadListener(final AddReloadListenerEvent event) {
    event.addListener(new MachineJsonReloadListener());
  }

  @Contract("_ -> new")
  public static @NotNull ResourceLocation rl(String path) {
    return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase(Locale.ROOT));
  }

  public static String rootLC(String s) {
    return s.toLowerCase(Locale.ROOT);
  }

  public static String rootUC(String s) {
    return s.toUpperCase(Locale.ROOT);
  }

  private void registerCommands(final RegisterCommandsEvent event) {
    event.getDispatcher().register(MMRCommand.register("modularmachineryreborn"));
    event.getDispatcher().register(MMRCommand.register("modularmachinery"));
    event.getDispatcher().register(MMRCommand.register("modular_machinery_reborn"));
    event.getDispatcher().register(MMRCommand.register("modular_machinery"));
    event.getDispatcher().register(MMRCommand.register("mmr"));
    event.getDispatcher().register(MMRCommand.register("mm"));
  }

  private void onReloadStart(final CommandEvent event) {
    if (event.getParseResults().getReader().getString().equals("reload") && event.getParseResults().getContext().getSource().hasPermission(2)) {
      MMRLogger.reset();
      ConfigLoaded.load();
      Config.load();
      EnergyDisplayUtil.loadFromConfig();
      if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
        MMRCommand.reloadMachines(player.server, player);
      }
    }
  }

  public static Registry<ProcessorType<? extends IProcessor>> processorRegistrar() {
    return ProcessorTypeRegistration.PROCESSOR_REGISTRY;
  }

  public static Registry<RequirementType<? extends IRequirement<?>>> getRequirementRegistrar() {
    return RequirementTypeRegistration.REQUIREMENTS_REGISTRY;
  }

  public static Registry<ComponentType> getComponentRegistrar() {
    return ComponentRegistration.COMPONENTS_REGISTRY;
  }
  public static Registry<MachineHatchType> getMachineHatchTypeRegistrar() {
    return MachineHatchTypeRegistration.MachineHatchType_REGISTRY;
  }

  public static Registry<EmptyRequirementType> getEmptyRequirementTypeRegistrar() {
    return EmptyRequirementTypeRegistration.EMPTY_REQUIREMENT_REGISTRY;
  }

  public static Registry<DataType<? extends IData<?>, ?>> dataRegistrar() {
    return DataRegistration.DATA_REGISTRY;
  }
}
