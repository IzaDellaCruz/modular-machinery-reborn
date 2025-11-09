package es.degrassi.mmreborn.data;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.block.BlockCasing;
import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.block.BlockMachineComponent;
import es.degrassi.mmreborn.common.block.prop.FuelTankSize;
import es.degrassi.mmreborn.common.registration.BlockRegistration;
import es.degrassi.mmreborn.data.blockstate.builder.StateCasingBuilder;
import es.degrassi.mmreborn.data.blockstate.builder.StateControllerBuilder;
import es.degrassi.mmreborn.data.blockstate.builder.StateHatchBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MMRBlockStateProvider extends BlockStateProvider {
  private static final ResourceLocation empty = ModularMachineryReborn.rl("block/casing_empty");
  private static final ResourceLocation plainCenter = ModularMachineryReborn.rl("block/casing_plain_corners");
  private static final ResourceLocation plainHorizontal = ModularMachineryReborn.rl("block/casing_plain_horizontal");
  private static final ResourceLocation plainVertical = ModularMachineryReborn.rl("block/casing_plain_vertical");
  private static final ResourceLocation plainParticle = ModularMachineryReborn.rl("block/casing_plain");
  private static final ResourceLocation reinforcedCenter = ModularMachineryReborn.rl("block/casing_reinforced_corners");
  private static final ResourceLocation reinforcedHorizontal = ModularMachineryReborn.rl("block/casing_reinforced_horizontal");
  private static final ResourceLocation reinforcedVertical = ModularMachineryReborn.rl("block/casing_reinforced_vertical");
  private static final ResourceLocation reinforcedParticle = ModularMachineryReborn.rl("block/casing_reinforced");

  private ResourceLocation key(Block block) {
    return BuiltInRegistries.BLOCK.getKey(block);
  }

  public MMRBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, ModularMachineryReborn.MODID, exFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    addController(BlockRegistration.CONTROLLER.get(), false, ModularMachineryReborn.rl("block/overlay_controller"));
    addCasing(BlockRegistration.CASING_PLAIN.get(), false, null, modLoc("block/overlay_transparent"));
    addCasing(BlockRegistration.CASING_REINFORCED.get(), true, null, modLoc("block/overlay_reinforced"));
    addCasing(BlockRegistration.CASING_CIRCUITRY.get(), false, ModularMachineryReborn.rl("block/overlay_circuitry"), modLoc("block/overlay_circuitry"));
    addCasing(BlockRegistration.CASING_GEARBOX.get(), false, ModularMachineryReborn.rl("block/overlay_gearbox"), modLoc("block/overlay_gearbox"));
    addCasing(BlockRegistration.CASING_VENT.get(), false, ModularMachineryReborn.rl("block/overlay_vent"), modLoc("block/overlay_vent"));
    addCasing(BlockRegistration.CASING_FIREBOX.get(), false, ModularMachineryReborn.rl("block/overlay_firebox"), modLoc("block/overlay_firebox"));
    addHatch(BlockRegistration.FUEL_TANK_TINY.get(), false, ModularMachineryReborn.rl("block/overlay_fueltank_" + FuelTankSize.TINY.getSerializedName()), false);
    addHatch(BlockRegistration.FUEL_TANK_SMALL.get(), false, ModularMachineryReborn.rl("block/overlay_fueltank_" + FuelTankSize.SMALL.getSerializedName()), false);
    addHatch(BlockRegistration.FUEL_TANK_NORMAL.get(), false, ModularMachineryReborn.rl("block/overlay_fueltank_" + FuelTankSize.NORMAL.getSerializedName()), false);
    addHatch(BlockRegistration.FUEL_TANK_REINFORCED.get(), true, ModularMachineryReborn.rl("block/overlay_fueltank_" + FuelTankSize.REINFORCED.getSerializedName()), false);
    addHatch(BlockRegistration.FUEL_TANK_BIG.get(), true, ModularMachineryReborn.rl("block/overlay_fueltank_" + FuelTankSize.BIG.getSerializedName()), false);
    addHatch(BlockRegistration.FUEL_TANK_HUGE.get(), true, ModularMachineryReborn.rl("block/overlay_fueltank_" + FuelTankSize.HUGE.getSerializedName()), false);
    addHatch(BlockRegistration.BIOME_READER.get(), false, modLoc("block/overlay_biome_reader"), false);
    addHatch(BlockRegistration.CHUNKLOADER.get(), false, modLoc("block/overlay_chunkloader"), false);
    addHatch(BlockRegistration.DIMENSIONAL_DETECTOR.get(), false, modLoc("block/overlay_dimensional_detector"), false);

    addHatch(BlockRegistration.ITEM_DURABILITY_HATCH_TINY.get(), false, modLoc("block/overlay_durabilityhatch_tiny"), false);
    addHatch(BlockRegistration.ITEM_DURABILITY_HATCH_SMALL.get(), false, modLoc("block/overlay_durabilityhatch_small"), false);
    addHatch(BlockRegistration.ITEM_DURABILITY_HATCH_NORMAL.get(), false, modLoc("block/overlay_durabilityhatch_normal"), false);
    addHatch(BlockRegistration.ITEM_DURABILITY_HATCH_TINY.get(), true, modLoc("block/overlay_durabilityhatch_big"), false);

    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_TINY.get(), false, modLoc("block/overlay_energyinputhatch_tiny"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_SMALL.get(), false, modLoc("block/overlay_energyinputhatch_small"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_NORMAL.get(), false, modLoc("block/overlay_energyinputhatch_normal"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_REINFORCED.get(), true, modLoc("block/overlay_energyinputhatch_reinforced"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_BIG.get(), true, modLoc("block/overlay_energyinputhatch_big"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_HUGE.get(), true, modLoc("block/overlay_energyinputhatch_huge"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_LUDICROUS.get(), true, modLoc("block/overlay_energyinputhatch_ludicrous"), false);
    addHatch(BlockRegistration.ENERGY_INPUT_HATCH_ULTIMATE.get(), true, modLoc("block/overlay_energyinputhatch_ultimate"), false);

    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_TINY.get(), false, modLoc("block/overlay_energyoutputhatch_tiny"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_SMALL.get(), false, modLoc("block/overlay_energyoutputhatch_small"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_NORMAL.get(), false, modLoc("block/overlay_energyoutputhatch_normal"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_REINFORCED.get(), true, modLoc("block/overlay_energyoutputhatch_reinforced"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_BIG.get(), true, modLoc("block/overlay_energyoutputhatch_big"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_HUGE.get(), true, modLoc("block/overlay_energyoutputhatch_huge"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_LUDICROUS.get(), true, modLoc("block/overlay_energyoutputhatch_ludicrous"), false);
    addHatch(BlockRegistration.ENERGY_OUTPUT_HATCH_ULTIMATE.get(), true, modLoc("block/overlay_energyoutputhatch_ultimate"), false);
  }

  public void addCasing(BlockCasing block, boolean isReinforced, @Nullable ResourceLocation overlay, ResourceLocation ovAll) {
    var builder = new StateCasingBuilder()
        .center(plainCenter)
        .horizontal(plainHorizontal)
        .empty(empty)
        .vertical(plainVertical)
        .particle(plainParticle);
    if (isReinforced) {
      builder = builder.connectToReinforced()
          .ovCenter(reinforcedCenter)
          .ovHorizontal(reinforcedHorizontal)
          .ovVertical(reinforcedVertical)
          .ovParticle(reinforcedParticle);
    } else {
      builder = builder.connectToPlain();
    }
    if (overlay != null) {
      builder = builder.overlay(overlay);
    }
    this.registeredBlocks.put(block, builder);
    item(block, false, false, ovAll);
  }

  public void addController(BlockController block, boolean isReinforced, @Nullable ResourceLocation overlay) {
    var builder = new StateControllerBuilder()
        .center(plainCenter)
        .horizontal(plainHorizontal)
        .empty(empty)
        .vertical(plainVertical)
        .particle(plainParticle);
    if (isReinforced) {
      builder = builder.connectToReinforced()
          .ovCenter(reinforcedCenter)
          .ovHorizontal(reinforcedHorizontal)
          .ovVertical(reinforcedVertical)
          .ovParticle(reinforcedParticle);
    } else {
      builder = builder.connectToPlain();
    }
    if (overlay != null) {
      builder = builder.overlay(overlay);
    }
    this.registeredBlocks.put(block, builder);
    item(block, true, true, null);
  }


  public void addHatch(BlockMachineComponent block, boolean isReinforced, @Nullable ResourceLocation overlay, boolean orientable) {
    var builder = new StateHatchBuilder()
        .center(plainCenter)
        .horizontal(plainHorizontal)
        .empty(empty)
        .vertical(plainVertical)
        .particle(plainParticle);
    if (isReinforced) {
      builder = builder.connectToReinforced()
          .ovCenter(reinforcedCenter)
          .ovHorizontal(reinforcedHorizontal)
          .ovVertical(reinforcedVertical)
          .ovParticle(reinforcedParticle);
    } else {
      builder = builder.connectToPlain();
    }
    if (overlay != null) {
      builder = builder.overlay(overlay);
    }
    this.registeredBlocks.put(block, builder);
    item(block, orientable, true, null);
  }

  public ResourceLocation loader(Block block) {
    return switch (block) {
      case BlockController blockController -> ModularMachineryReborn.rl("controller");
      case BlockMachineComponent blockMachineComponent -> ModularMachineryReborn.rl("hatch");
      default -> null;
    };
  }

  public void item(Block block, boolean orientable, boolean loader, @Nullable ResourceLocation ovAll) {
    BlockModelBuilder model;
    if (loader) {
      model = this.models()
          .getBuilder(this.key(block).toString())
          .customLoader((builder, helper) -> new CustomLoaderBuilder<BlockModelBuilder>(
              loader(block),
              builder,
              helper,
              false
          ) {})
          .end();
      // new ModelFile.UncheckedModelFile(this.key(block).withPrefix("block/").toString())
    } else {
      assert ovAll != null;
      model = models()
          .getBuilder(this.key(block).toString())
          .parent(orientable
              ? new ModelFile.UncheckedModelFile(ModularMachineryReborn.rl("blockmodel_overlay_orientable_all").withPrefix("block/").toString())
              : new ModelFile.UncheckedModelFile(ModularMachineryReborn.rl("blockmodel_overlay_all").withPrefix("block/").toString())
          ).texture("bg_all", plainParticle)
          .texture("ov_all", ovAll);
    }
    simpleBlockWithItem(block, model);
  }

  public void simpleBlockWithItem(Block block, BlockModelBuilder model) {
    this.simpleBlock(block, model);
    this.simpleBlockItem(block, model);
  }

  public void simpleBlock(Block block, BlockModelBuilder model) {
    models().generatedModels.put(this.key(block), model);
  }
}
