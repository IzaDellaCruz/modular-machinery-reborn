package es.degrassi.mmreborn.data;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.block.BlockCasing;
import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.block.BlockMachineComponent;
import es.degrassi.mmreborn.data.blockstate.builder.StateCasingBuilder;
import es.degrassi.mmreborn.data.blockstate.builder.StateControllerBuilder;
import es.degrassi.mmreborn.data.blockstate.builder.StateHatchBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BaseMMRBlockStateProvider extends BlockStateProvider {
  protected static final ResourceLocation empty = ModularMachineryReborn.rl("block/casing_empty");
  protected static final ResourceLocation plainCenter = ModularMachineryReborn.rl("block/casing_plain_corners");
  protected static final ResourceLocation plainHorizontal = ModularMachineryReborn.rl("block/casing_plain_horizontal");
  protected static final ResourceLocation plainVertical = ModularMachineryReborn.rl("block/casing_plain_vertical");
  protected static final ResourceLocation plainParticle = ModularMachineryReborn.rl("block/casing_plain");
  protected static final ResourceLocation reinforcedCenter = ModularMachineryReborn.rl("block/casing_reinforced_corners");
  protected static final ResourceLocation reinforcedHorizontal = ModularMachineryReborn.rl("block/casing_reinforced_horizontal");
  protected static final ResourceLocation reinforcedVertical = ModularMachineryReborn.rl("block/casing_reinforced_vertical");
  protected static final ResourceLocation reinforcedParticle = ModularMachineryReborn.rl("block/casing_reinforced");

  protected ResourceLocation key(Block block) {
    return BuiltInRegistries.BLOCK.getKey(block);
  }

  protected BaseMMRBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
    super(output, modid, exFileHelper);
  }

  protected void addCasing(BlockCasing block, boolean isReinforced, @Nullable ResourceLocation overlay, ResourceLocation ovAll) {
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
  protected void addController(BlockController block, boolean isReinforced, ResourceLocation overlay) {
    var builder = new StateControllerBuilder()
        .center(plainCenter)
        .horizontal(plainHorizontal)
        .empty(empty)
        .vertical(plainVertical)
        .particle(plainParticle)
        .overlay(overlay);
    var defaultControllerBuilder = models()
        .getBuilder(this.key(block).withPrefix("default/").toString())
        .parent(new ModelFile.UncheckedModelFile(modLoc("block/blockmodel_overlay_orientable_all").toString()))
        .texture("bg_all", plainParticle)
        .texture("ov_front", overlay)
        .texture("ov_side", modLoc("block/overlay_transparent"))
        .texture("ov_top", modLoc("block/overlay_transparent"));
    if (isReinforced) {
      builder = builder.connectToReinforced()
          .ovCenter(reinforcedCenter)
          .ovHorizontal(reinforcedHorizontal)
          .ovVertical(reinforcedVertical)
          .ovParticle(reinforcedParticle);
      defaultControllerBuilder = defaultControllerBuilder
          .texture("ov_r_all", reinforcedParticle);
    } else {
      builder = builder.connectToPlain();
    }
    this.registeredBlocks.put(block, builder);
    item(block, true, true, null);
    models().generatedModels.put(this.key(block).withPrefix("default/"), defaultControllerBuilder);
  }
  protected void addHatch(BlockMachineComponent block, boolean isReinforced, ResourceLocation overlay, boolean orientable) {
    var builder = new StateHatchBuilder()
        .center(plainCenter)
        .horizontal(plainHorizontal)
        .empty(empty)
        .vertical(plainVertical)
        .particle(plainParticle)
        .overlay(overlay);
    var defaultHatchBuilder = models()
        .getBuilder(this.key(block).withPrefix("default/hatches/").toString())
        .parent(orientable
            ? new ModelFile.UncheckedModelFile(modLoc("block/blockmodel_overlay_orientable_all").toString())
            : new ModelFile.UncheckedModelFile(modLoc("block/blockmodel_overlay_all").toString())
        ).texture("bg_all", plainParticle)
        .texture(orientable ? "ov_front" : "ov_all", overlay);
    if (orientable) {
      defaultHatchBuilder = defaultHatchBuilder
          .texture("ov_side", modLoc("block/overlay_transparent"))
          .texture("ov_top", modLoc("block/overlay_transparent"));
    }
    if (isReinforced) {
      builder = builder.connectToReinforced()
          .ovCenter(reinforcedCenter)
          .ovHorizontal(reinforcedHorizontal)
          .ovVertical(reinforcedVertical)
          .ovParticle(reinforcedParticle);
      defaultHatchBuilder = defaultHatchBuilder
          .texture("ov_r_all", reinforcedParticle);
    } else {
      builder = builder.connectToPlain();
    }
    this.registeredBlocks.put(block, builder);
    item(block, orientable, true, null);
    defaultHatch(block, defaultHatchBuilder);
  }
  protected ResourceLocation loader(Block block) {
    return switch (block) {
      case BlockController blockController -> ModularMachineryReborn.rl("controller");
      case BlockMachineComponent blockMachineComponent -> ModularMachineryReborn.rl("hatch");
      default -> null;
    };
  }
  protected void item(Block block, boolean orientable, boolean loader, @Nullable ResourceLocation ovAll) {
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
    } else {
      assert ovAll != null;
      model = models()
          .getBuilder(this.key(block).toString())
          .parent(orientable
              ? new ModelFile.UncheckedModelFile(modLoc("block/blockmodel_overlay_orientable_all").toString())
              : new ModelFile.UncheckedModelFile(modLoc("block/blockmodel_overlay_all").toString())
          ).texture("bg_all", plainParticle)
          .texture(orientable ? "ov_front" : "ov_all", ovAll);
      if (orientable) {
        model = model
            .texture("ov_side", modLoc("block/overlay_transparent"))
            .texture("ov_top", modLoc("block/overlay_transparent"));
      }
    }
    simpleBlockWithItem(block, model);
  }
  protected void simpleBlockWithItem(Block block, BlockModelBuilder model) {
    this.simpleBlock(block, model);
    this.simpleBlockItem(block, model);
  }
  protected void simpleBlock(Block block, BlockModelBuilder model) {
    models().generatedModels.put(this.key(block), model);
  }
  public void defaultHatch(Block block, BlockModelBuilder model) {
    models().generatedModels.put(this.key(block).withPrefix("default/hatches/"), model);
  }
  public void defaultModel(ResourceLocation path, BlockModelBuilder model) {
    models().generatedModels.put(path.withPrefix("default/"), model);
  }
  public void block(ResourceLocation path, BlockModelBuilder model) {
    models().generatedModels.put(path, model);
  }
  public void basicItem(Item item, ResourceLocation texture) {
    this.basicItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)), Objects.requireNonNull(texture));
  }
  public void basicItem(ResourceLocation item, ResourceLocation texture) {
    itemModels().getBuilder(item.toString()).parent(new ModelFile.UncheckedModelFile("item/generated")).texture("layer0", texture);
  }
}
