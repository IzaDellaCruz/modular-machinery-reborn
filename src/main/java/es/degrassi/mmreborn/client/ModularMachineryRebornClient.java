package es.degrassi.mmreborn.client;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.entity.renderer.ControllerRenderer;
import es.degrassi.mmreborn.client.integration.athena.MMRAthenaModels;
import es.degrassi.mmreborn.client.integration.emi.MMREmiClientIntegration;
import es.degrassi.mmreborn.client.integration.jei.MMRJeiClientIntegration;
import es.degrassi.mmreborn.client.item.MMRItemTooltip;
import es.degrassi.mmreborn.client.item.MMRItemTooltipComponent;
import es.degrassi.mmreborn.client.model.InitBuiltInModels;
import es.degrassi.mmreborn.client.model.controller.ControllerModelLoader;
import es.degrassi.mmreborn.client.model.hatch.HatchModelLoader;
import es.degrassi.mmreborn.client.screen.ControllerScreen;
import es.degrassi.mmreborn.client.screen.EnergyHatchScreen;
import es.degrassi.mmreborn.client.screen.ExperienceHatchScreen;
import es.degrassi.mmreborn.client.screen.FluidHatchScreen;
import es.degrassi.mmreborn.client.screen.ItemBusScreen;
import es.degrassi.mmreborn.client.screen.ItemDurabilityScreen;
import es.degrassi.mmreborn.client.screen.ParallelHatchScreen;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.entity.ParallelHatchEntity;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineEntity;
import es.degrassi.mmreborn.common.entity.base.EnergyHatchEntity;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.entity.base.FluidTankEntity;
import es.degrassi.mmreborn.common.entity.base.DurabilityHatchEntity;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import es.degrassi.mmreborn.common.item.ItemDynamicColor;
import es.degrassi.mmreborn.common.registration.BlockRegistration;
import es.degrassi.mmreborn.common.registration.ContainerRegistration;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import es.degrassi.mmreborn.common.util.Mods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Mod(value = ModularMachineryReborn.MODID, dist = Dist.CLIENT)
public class ModularMachineryRebornClient {
  public static final ClientScheduler clientScheduler = new ClientScheduler();
  private static Map<ModelResourceLocation, BakedModel> models;
  private static final List<Block> blockModelsToRegister = Lists.newLinkedList();
  private static final List<Item> itemModelsToRegister = Lists.newLinkedList();
  private final IEventBus bus;

  public ModularMachineryRebornClient(final IEventBus bus) {
    MMRAthenaModels.init();
    InitBuiltInModels.init();
    NeoForge.EVENT_BUS.register(clientScheduler);
    bus.register(this);
    this.bus = bus;
  }

  @SubscribeEvent
  public void registerBlockEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(EntityRegistration.CONTROLLER.get(), ControllerRenderer::new);
  }

  @SubscribeEvent
  public void registerModelLoader(final ModelEvent.RegisterGeometryLoaders event) {
    event.register(ModularMachineryReborn.rl("controller"), ControllerModelLoader.INSTANCE);
    event.register(ModularMachineryReborn.rl("hatch"), HatchModelLoader.INSTANCE);
  }

  @SubscribeEvent
  public void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
    event.register(MMRItemTooltipComponent.class, c -> new MMRItemTooltip(c.getItem(), c.getComponent(), c.isCompleted()));
  }

  @SubscribeEvent
  public void onBackingCompleted(final ModelEvent.BakingCompleted event) {
    models = event.getModels();
  }

  public static Map<ModelResourceLocation, BakedModel> getAllModels() {
    return models;
  }

  @SubscribeEvent
  public void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
    event.register(
        ModularMachineryRebornClient::blockColor,
        BlockRegistration.CONTROLLER.get(),

        BlockRegistration.CASING_PLAIN.get(),
        BlockRegistration.CASING_VENT.get(),
        BlockRegistration.CASING_FIREBOX.get(),
        BlockRegistration.CASING_GEARBOX.get(),
        BlockRegistration.CASING_REINFORCED.get(),
        BlockRegistration.CASING_CIRCUITRY.get(),

        BlockRegistration.ENERGY_INPUT_HATCH_TINY.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_SMALL.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_NORMAL.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_REINFORCED.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_BIG.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_HUGE.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_LUDICROUS.get(),
        BlockRegistration.ENERGY_INPUT_HATCH_ULTIMATE.get(),

        BlockRegistration.ENERGY_OUTPUT_HATCH_TINY.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_SMALL.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_NORMAL.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_REINFORCED.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_BIG.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_HUGE.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_LUDICROUS.get(),
        BlockRegistration.ENERGY_OUTPUT_HATCH_ULTIMATE.get(),

        BlockRegistration.ITEM_INPUT_BUS_TINY.get(),
        BlockRegistration.ITEM_INPUT_BUS_SMALL.get(),
        BlockRegistration.ITEM_INPUT_BUS_NORMAL.get(),
        BlockRegistration.ITEM_INPUT_BUS_REINFORCED.get(),
        BlockRegistration.ITEM_INPUT_BUS_BIG.get(),
        BlockRegistration.ITEM_INPUT_BUS_HUGE.get(),
        BlockRegistration.ITEM_INPUT_BUS_LUDICROUS.get(),

        BlockRegistration.ITEM_OUTPUT_BUS_TINY.get(),
        BlockRegistration.ITEM_OUTPUT_BUS_SMALL.get(),
        BlockRegistration.ITEM_OUTPUT_BUS_NORMAL.get(),
        BlockRegistration.ITEM_OUTPUT_BUS_REINFORCED.get(),
        BlockRegistration.ITEM_OUTPUT_BUS_BIG.get(),
        BlockRegistration.ITEM_OUTPUT_BUS_HUGE.get(),
        BlockRegistration.ITEM_OUTPUT_BUS_LUDICROUS.get(),

        BlockRegistration.ITEM_DURABILITY_HATCH_TINY.get(),
        BlockRegistration.ITEM_DURABILITY_HATCH_SMALL.get(),
        BlockRegistration.ITEM_DURABILITY_HATCH_NORMAL.get(),
        BlockRegistration.ITEM_DURABILITY_HATCH_BIG.get(),

        BlockRegistration.FLUID_INPUT_HATCH_TINY.get(),
        BlockRegistration.FLUID_INPUT_HATCH_SMALL.get(),
        BlockRegistration.FLUID_INPUT_HATCH_NORMAL.get(),
        BlockRegistration.FLUID_INPUT_HATCH_REINFORCED.get(),
        BlockRegistration.FLUID_INPUT_HATCH_BIG.get(),
        BlockRegistration.FLUID_INPUT_HATCH_HUGE.get(),
        BlockRegistration.FLUID_INPUT_HATCH_LUDICROUS.get(),
        BlockRegistration.FLUID_INPUT_HATCH_VACUUM.get(),

        BlockRegistration.FLUID_OUTPUT_HATCH_TINY.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_SMALL.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_NORMAL.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_REINFORCED.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_BIG.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_HUGE.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_LUDICROUS.get(),
        BlockRegistration.FLUID_OUTPUT_HATCH_VACUUM.get(),

        BlockRegistration.EXPERIENCE_INPUT_HATCH_TINY.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_SMALL.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_NORMAL.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_REINFORCED.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_BIG.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_HUGE.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_LUDICROUS.get(),
        BlockRegistration.EXPERIENCE_INPUT_HATCH_VACUUM.get(),

        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_TINY.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_SMALL.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_NORMAL.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_REINFORCED.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_BIG.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_HUGE.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_LUDICROUS.get(),
        BlockRegistration.EXPERIENCE_OUTPUT_HATCH_VACUUM.get(),

        BlockRegistration.DIMENSIONAL_DETECTOR.get(),
        BlockRegistration.BIOME_READER.get(),
        BlockRegistration.WEATHER_SENSOR.get(),
        BlockRegistration.TIME_COUNTER.get(),
        BlockRegistration.CHUNKLOADER.get(),
        BlockRegistration.HEIGHT_METER.get(),

        BlockRegistration.PARALLEL_HATCH_BASIC.get(),
        BlockRegistration.PARALLEL_HATCH_MEDIUM.get(),
        BlockRegistration.PARALLEL_HATCH_ADVANCED.get(),
        BlockRegistration.PARALLEL_HATCH_ULTIMATE.get(),
        BlockRegistration.PARALLEL_HATCH_MAX.get()
    );
    ModularMachineryReborn.MACHINES_BLOCK.values().forEach(block -> event.register(ModularMachineryRebornClient::blockColor, block));
  }

  @SubscribeEvent
  public void registerItemColors(final RegisterColorHandlersEvent.Item event) {
    event.register(
        ModularMachineryRebornClient::itemColor,
        ItemRegistration.MODULARIUM.get(),

        ItemRegistration.CONTROLLER.get(),

        ItemRegistration.CASING_PLAIN.get(),
        ItemRegistration.CASING_VENT.get(),
        ItemRegistration.CASING_FIREBOX.get(),
        ItemRegistration.CASING_GEARBOX.get(),
        ItemRegistration.CASING_REINFORCED.get(),
        ItemRegistration.CASING_CIRCUITRY.get(),

        ItemRegistration.ENERGY_INPUT_HATCH_TINY.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_SMALL.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_NORMAL.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_REINFORCED.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_BIG.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_HUGE.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_LUDICROUS.get(),
        ItemRegistration.ENERGY_INPUT_HATCH_ULTIMATE.get(),

        ItemRegistration.ENERGY_OUTPUT_HATCH_TINY.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_SMALL.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_NORMAL.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_REINFORCED.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_BIG.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_HUGE.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_LUDICROUS.get(),
        ItemRegistration.ENERGY_OUTPUT_HATCH_ULTIMATE.get(),

        ItemRegistration.ITEM_INPUT_BUS_TINY.get(),
        ItemRegistration.ITEM_INPUT_BUS_SMALL.get(),
        ItemRegistration.ITEM_INPUT_BUS_NORMAL.get(),
        ItemRegistration.ITEM_INPUT_BUS_REINFORCED.get(),
        ItemRegistration.ITEM_INPUT_BUS_BIG.get(),
        ItemRegistration.ITEM_INPUT_BUS_HUGE.get(),
        ItemRegistration.ITEM_INPUT_BUS_LUDICROUS.get(),

        ItemRegistration.ITEM_OUTPUT_BUS_TINY.get(),
        ItemRegistration.ITEM_OUTPUT_BUS_SMALL.get(),
        ItemRegistration.ITEM_OUTPUT_BUS_NORMAL.get(),
        ItemRegistration.ITEM_OUTPUT_BUS_REINFORCED.get(),
        ItemRegistration.ITEM_OUTPUT_BUS_BIG.get(),
        ItemRegistration.ITEM_OUTPUT_BUS_HUGE.get(),
        ItemRegistration.ITEM_OUTPUT_BUS_LUDICROUS.get(),

        ItemRegistration.ITEM_DURABILITY_HATCH_TINY.get(),
        ItemRegistration.ITEM_DURABILITY_HATCH_SMALL.get(),
        ItemRegistration.ITEM_DURABILITY_HATCH_NORMAL.get(),
        ItemRegistration.ITEM_DURABILITY_HATCH_BIG.get(),

        ItemRegistration.FLUID_INPUT_HATCH_TINY.get(),
        ItemRegistration.FLUID_INPUT_HATCH_SMALL.get(),
        ItemRegistration.FLUID_INPUT_HATCH_NORMAL.get(),
        ItemRegistration.FLUID_INPUT_HATCH_REINFORCED.get(),
        ItemRegistration.FLUID_INPUT_HATCH_BIG.get(),
        ItemRegistration.FLUID_INPUT_HATCH_HUGE.get(),
        ItemRegistration.FLUID_INPUT_HATCH_LUDICROUS.get(),
        ItemRegistration.FLUID_INPUT_HATCH_VACUUM.get(),

        ItemRegistration.FLUID_OUTPUT_HATCH_TINY.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_SMALL.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_NORMAL.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_REINFORCED.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_BIG.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_HUGE.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_LUDICROUS.get(),
        ItemRegistration.FLUID_OUTPUT_HATCH_VACUUM.get(),

        ItemRegistration.EXPERIENCE_INPUT_HATCH_TINY.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_SMALL.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_NORMAL.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_REINFORCED.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_BIG.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_HUGE.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_LUDICROUS.get(),
        ItemRegistration.EXPERIENCE_INPUT_HATCH_VACUUM.get(),

        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_TINY.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_SMALL.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_NORMAL.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_REINFORCED.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_BIG.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_HUGE.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_LUDICROUS.get(),
        ItemRegistration.EXPERIENCE_OUTPUT_HATCH_VACUUM.get(),

        ItemRegistration.DIMENSIONAL_DETECTOR.get(),
        ItemRegistration.BIOME_READER.get(),
        ItemRegistration.WEATHER_SENSOR.get(),
        ItemRegistration.TIME_COUNTER.get(),
        ItemRegistration.CHUNKLOADER.get(),
        ItemRegistration.HEIGHT_METER.get(),

        ItemRegistration.PARALLEL_HATCH_BASIC.get(),
        ItemRegistration.PARALLEL_HATCH_MEDIUM.get(),
        ItemRegistration.PARALLEL_HATCH_ADVANCED.get(),
        ItemRegistration.PARALLEL_HATCH_ULTIMATE.get(),
        ItemRegistration.PARALLEL_HATCH_MAX.get()
    );
    ModularMachineryReborn.MACHINES_BLOCK.values().forEach(block -> event.register(ModularMachineryRebornClient::itemColor, block));
  }

  public static int blockColor(BlockState state, @Nullable  BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
    if (level == null || pos == null)
      return 0;
    switch (tintIndex) {
      case 1 -> level.getBlockTint(pos, BiomeColors.WATER_COLOR_RESOLVER);
      case 2 -> level.getBlockTint(pos, BiomeColors.GRASS_COLOR_RESOLVER);
      case 3 -> level.getBlockTint(pos, BiomeColors.FOLIAGE_COLOR_RESOLVER);
      case 4 -> {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof ColorableMachineEntity machineTile) {
          return machineTile.getMachineColor();
        }
      }
    }
    return Config.machineColor;
  }

  public static int itemColor(ItemStack stack, int tintIndex) {
    if (stack.getItem() instanceof ItemDynamicColor colorableItem) {
      return colorableItem.getColorFromItemstack(stack, tintIndex);
    }
    return Config.machineColor;
  }

  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public void onModelRegister(ModelEvent.RegisterAdditional event) {
    event.register(ModelResourceLocation.standalone(ModularMachineryReborn.rl("block/nope")));
    event.register(ModelResourceLocation.standalone(ModularMachineryReborn.rl("default/controller")));
    event.register(ModelResourceLocation.standalone(ModularMachineryReborn.rl("default/hatch_all")));
    event.register(ModelResourceLocation.standalone(ModularMachineryReborn.rl("default/hatch_orientable")));
    Minecraft.getInstance().getResourceManager().listResources("models/default/hatches", s -> s.getPath().endsWith(".json")).forEach((rl, resource) -> {
      ResourceLocation modelRL = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
      event.register(ModelResourceLocation.standalone(modelRL));
    });
    for (String folder : MMRConfig.get().modelFolders.get()) {
      Minecraft.getInstance().getResourceManager().listResources("models/" + folder, s -> s.getPath().endsWith(".json")).forEach((rl, resource) -> {
        ResourceLocation modelRL = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
        event.register(ModelResourceLocation.standalone(modelRL));
      });
    }
    for (Block block : blockModelsToRegister) {
      Item i = block.asItem();
    }
    for (Item item : itemModelsToRegister) {
      String name = item.getClass().getSimpleName().toLowerCase();
      if (item instanceof BlockItem) {
        name = ((BlockItem) item).getBlock().getClass().getSimpleName().toLowerCase();
      }
      event.register(new ModelResourceLocation(ModularMachineryReborn.rl(name), "inventory"));
    }
  }

  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public void clientSetup(final FMLClientSetupEvent event) {
    if (Mods.isEMILoaded()) {
      new MMREmiClientIntegration(bus);
    } else if (Mods.isJEILoaded()) {
      new MMRJeiClientIntegration(bus);
    }
  }

  public void registerBlockModel(Block block) {
    blockModelsToRegister.add(block);
  }

  public void registerItemModel(Item item) {
    itemModelsToRegister.add(item);
  }

  @SubscribeEvent
  public void registerMenuScreens(final RegisterMenuScreensEvent event) {
    event.register(ContainerRegistration.CONTROLLER.get(), ControllerScreen::new);
    event.register(ContainerRegistration.ENERGY_HATCH.get(), EnergyHatchScreen::new);
    event.register(ContainerRegistration.FLUID_HATCH.get(), FluidHatchScreen::new);
    event.register(ContainerRegistration.ITEM_BUS.get(), ItemBusScreen::new);
    event.register(ContainerRegistration.ITEM_DURABILITY_HATCH.get(), ItemDurabilityScreen::new);
    event.register(ContainerRegistration.EXPERIENCE_HATCH.get(), ExperienceHatchScreen::new);
    event.register(ContainerRegistration.PARALLEL_HATCH.get(), ParallelHatchScreen::new);
  }

  @NotNull
  public static MachineControllerEntity getClientSideMachineControllerEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof MachineControllerEntity controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Controller container without clicking on a Controller block");
  }

  public static EnergyHatchEntity getClientSideEnergyHatchEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof EnergyHatchEntity controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Energy Hatch container without clicking on a Energy Hatch block");
  }

  public static FluidTankEntity getClientSideFluidHatchEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof FluidTankEntity controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Fluid Hatch container without clicking on a Fluid Hatch block");
  }

  public static TileItemBus getClientSideItemBusEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof TileItemBus controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Item Bus container without clicking on a Item Bus block");
  }

  public static ExperienceHatchEntity getClientSideExperienceHatchEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof ExperienceHatchEntity controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Experience Hatch container without clicking on a Experience Hatch block");
  }

  public static ParallelHatchEntity getClientSideParallelHatchEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof ParallelHatchEntity controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Parallel Hatch container without clicking on a Parallel Hatch block");
  }

  public static DurabilityHatchEntity getClientSideItemDurabilityEntity(BlockPos pos) {
    if (Minecraft.getInstance().level != null) {
      BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
      if (tile instanceof DurabilityHatchEntity controller)
        return controller;
    }
    throw new IllegalStateException("Trying to open a Durability Hatch container without clicking on a Durability Hatch block");
  }
}
