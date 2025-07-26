package es.degrassi.mmreborn.data;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class MMRTags {
  private MMRTags() {}
  private static TagKey<Block> blockTag(String name, boolean isNeoForge) {
    return BlockTags.create(isNeoForge ? ResourceLocation.fromNamespaceAndPath("c", name) : ModularMachineryReborn.rl(name));
  }

  private static TagKey<Item> itemTag(String name, boolean isNeoForge) {
    return ItemTags.create(isNeoForge ? ResourceLocation.fromNamespaceAndPath("c", name) : ModularMachineryReborn.rl(name));
  }

  private static class Tag<T> {
    private final TagKey<T> tag;
    protected Tag(TagKey<T> tag) {
      this.tag = tag;
    }

    public TagKey<T> get() {
      return tag;
    }
  }

  public static class Blocks extends Tag<Block> {
    public static final TagKey<Block> ENERGY = new Blocks(false, "energyhatch").get();
    public static final TagKey<Block> ENERGY_INPUT = new Blocks(false, "energyinputhatch").get();
    public static final TagKey<Block> ENERGY_OUTPUT = new Blocks(false, "energyoutputhatch").get();

    public static final TagKey<Block> ITEM = new Blocks(false, "itembus").get();
    public static final TagKey<Block> INPUT_BUS = new Blocks(false, "inputbus").get();
    public static final TagKey<Block> OUTPUT_BUS = new Blocks(false, "outputbus").get();

    public static final TagKey<Block> DURABILITY = new Blocks(false, "durability_hatch").get();

    public static final TagKey<Block> FLUID = new Blocks(false, "fluidhatch").get();
    public static final TagKey<Block> FLUID_INPUT = new Blocks(false, "fluidinputhatch").get();
    public static final TagKey<Block> FLUID_OUTPUT = new Blocks(false, "fluidoutputhatch").get();

    public static final TagKey<Block> EXPERIENCE = new Blocks(false, "experiencehatch").get();
    public static final TagKey<Block> EXPERIENCE_INPUT = new Blocks(false, "experienceinputhatch").get();
    public static final TagKey<Block> EXPERIENCE_OUTPUT = new Blocks(false, "experienceoutputhatch").get();

    public static final TagKey<Block> PARALLEL = new Blocks(false, "parallelhatch").get();

    public static final TagKey<Block> CASINGS = new Blocks(false, "casing").get();
    public static final TagKey<Block> ALL_CASINGS = new Blocks(false, "all_casing").get();

    public static final TagKey<Block> REPLACEABLE = new Blocks(false, "replaceable").get();

    private Blocks(boolean isNeoForge, String name) {
      super(blockTag(name, isNeoForge));
    }
  }

  public static class Items extends Tag<Item> {
    public static final TagKey<Item> ENERGY = new Items(false, "energyhatch").get();
    public static final TagKey<Item> ENERGY_INPUT = new Items(false, "energyinputhatch").get();
    public static final TagKey<Item> ENERGY_OUTPUT = new Items(false, "energyoutputhatch").get();

    public static final TagKey<Item> ITEM = new Items(false, "itembus").get();
    public static final TagKey<Item> INPUT_BUS = new Items(false, "inputbus").get();
    public static final TagKey<Item> OUTPUT_BUS = new Items(false, "outputbus").get();

    public static final TagKey<Item> DURABILITY = new Items(false, "durability_hatch").get();

    public static final TagKey<Item> FLUID = new Items(false, "fluidhatch").get();
    public static final TagKey<Item> FLUID_INPUT = new Items(false, "fluidinputhatch").get();
    public static final TagKey<Item> FLUID_OUTPUT = new Items(false, "fluidoutputhatch").get();

    public static final TagKey<Item> EXPERIENCE = new Items(false, "experiencehatch").get();
    public static final TagKey<Item> EXPERIENCE_INPUT = new Items(false, "experienceinputhatch").get();
    public static final TagKey<Item> EXPERIENCE_OUTPUT = new Items(false, "experienceoutputhatch").get();

    public static final TagKey<Item> PARALLEL = new Items(false, "parallelhatch").get();

    public static final TagKey<Item> CASINGS = new Items(false, "casing").get();
    public static final TagKey<Item> ALL_CASINGS = new Items(false, "all_casing").get();

    private Items(boolean isNeoForge, String name) {
      super(itemTag(name, isNeoForge));
    }
  }
}
