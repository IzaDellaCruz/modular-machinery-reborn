package es.degrassi.mmreborn.api;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockIngredient implements IIngredient<PartialBlockState> {
  public static final BlockIngredient AIR = new BlockIngredient(PartialBlockState.AIR);
  public static final BlockIngredient ANY = new BlockIngredient(PartialBlockState.ANY);
  public static final BlockIngredient MACHINE = new BlockIngredient(PartialBlockState.MACHINE);
  public static final BlockIngredient NOT_MACHINE = new BlockIngredient(PartialBlockState.NOT_MACHINE);

  public static final NamedCodec<BlockIngredient> TAG_CODEC = NamedCodec.STRING.comapFlatMap(string -> {
    try {
      return DataResult.success(BlockIngredient.create(string));
    } catch (IllegalArgumentException e) {
      return DataResult.error(e::getMessage);
    }
  }, BlockIngredient::getString, "BlockIngredient with tag");

  public static final NamedCodec<BlockIngredient> CODEC = NamedCodec.either(
      PartialBlockState.CODEC,
      TAG_CODEC,
      "Block Ingredient"
  ).listOf().flatComapMap(
      list -> {
        List<BlockIngredient> ings = Lists.newArrayList();
        list.forEach(either -> ings.add(either.map(BlockIngredient::new, Function.identity())));
        AtomicReference<BlockIngredient> ing = new AtomicReference<>(null);
        ings.iterator().forEachRemaining(i -> {
          if (ing.get() == null) {
            ing.set(i);
            return;
          }
          ing.set(ing.get().merge(i));
        });
        return ing.get();
      },
      ing -> {
        List<Either<PartialBlockState, BlockIngredient>> list = Lists.newArrayList();
        list.add(Either.right(ing));
        return DataResult.success(list);
      },
      "Block Ingredient"
  );

  private final Supplier<List<PartialBlockState>> partialBlockStates;
  @Getter
  private final boolean isTag;
  @Getter
  @Setter
  private List<TagKey<Block>> tags = Lists.newArrayList();

  public BlockIngredient(List<TagKey<Block>> tags, List<PartialBlockState> states) {
    List<PartialBlockState> statesCopy = Lists.newArrayList(states);
    this.isTag = !tags.isEmpty();
    if (isTag) {
      this.tags.addAll(tags);
      tags.forEach(tag ->
          statesCopy.addAll(TagUtil.getBlocks(tag)
              .map(PartialBlockState::new)
              .toList())
      );
    }
    this.partialBlockStates = Suppliers.memoize(() -> ImmutableList.copyOf(statesCopy));
  }

  public BlockIngredient(PartialBlockState partialBlockState) {
    this(Collections.emptyList(), Collections.singletonList(partialBlockState));
  }

  public static BlockIngredient create(String s) throws IllegalArgumentException {
    if (s.startsWith("[")) {
      if (s.endsWith("]")) {
        s = s.substring(1, s.length() - 1);
      } else {
        s = s.substring(1);
      }
    }
    String[] arr = s.split(", ");
    List<TagKey<Block>> tags = Lists.newArrayList();
    List<PartialBlockState> states = Lists.newArrayList();
    Arrays.asList(arr).forEach(string -> {
      if (string.startsWith("#")) {
        string = string.substring(1);
        if (!Utils.isResourceNameValid(string))
          throw new IllegalArgumentException(String.format("Invalid tag id : %s", string));
        tags.add(TagKey.create(Registries.BLOCK, ResourceLocation.parse(string)));
      } else {
        try {
          DataResult<PartialBlockState> result = PartialBlockState.CODEC.decode(JsonOps.INSTANCE, JsonOps.INSTANCE.createString(string)).map(Pair::getFirst);
          states.add(result.getOrThrow());
        } catch (Exception exception) {
          throw new IllegalArgumentException(String.format("Invalid block id: %s", string));
        }
      }
    });
    if (tags.isEmpty() && states.isEmpty())
      throw new IllegalArgumentException("Invalid tags or states provided, expected min size of 1");

    return new BlockIngredient(tags, states);
  }

  public BlockIngredient copy() {
    return new BlockIngredient(
        tags.stream()
            .map(TagKey::location)
            .map(tag -> TagKey.create(BuiltInRegistries.BLOCK.key(), tag))
            .toList(),
        partialBlockStates.get()
            .stream()
            .map(PartialBlockState::copy)
            .toList()
    );
  }

  @Override
  public List<PartialBlockState> getAll() {
    return this.partialBlockStates.get();
  }

  @Override
  public boolean test(PartialBlockState partialBlockState) {
    return this.partialBlockStates.get().stream().anyMatch(state -> state.getBlockState() == partialBlockState.getBlockState());
  }

  public List<ItemStack> getStacks(int amount) {
    List<ItemStack> stacks = getTagStacks(amount);
    stacks.addAll(getNonTagStacks(amount));
    return stacks
        .stream()
        .collect(Collectors.groupingBy(ItemStack::getItem, Collectors.summingInt(ItemStack::getCount)))
        .entrySet()
        .stream()
        .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
        .toList();
  }

  public List<ItemStack> getNonTagStacks(int amount) {
    return uniqueStates()
        .map(PartialBlockState::getBlockState)
        .map(BlockState::getBlock)
        .map(Block::asItem)
        .map(Item::getDefaultInstance)
        .map(stack -> stack.copyWithCount(amount))
        .toList();
  }

  public List<ItemStack> getTagStacks(int amount) {
    return Lists.newArrayList(
        getTags()
            .stream()
            .flatMap(TagUtil::getBlocks)
            .map(Block::asItem)
            .map(Item::getDefaultInstance)
            .map(stack -> stack.copyWithCount(amount))
            .iterator()
    );
  }

  public Stream<PartialBlockState> uniqueStates() {
    return getAll()
        .stream()
        .filter(state -> tags.stream().noneMatch(tag -> state.getBlockState().is(tag)));
  }

  public List<Component> getNames() {
    List<Component> ingredients = Lists.newArrayList();
    ingredients.addAll(this.tags.stream().map(TagKey::location).map(ResourceLocation::toString).map(s -> "#" + s).map(Component::literal).toList());

    ingredients.addAll(
        uniqueStates()
            .map(PartialBlockState::getName)
            .toList()
    );

    return ingredients;
  }

  public boolean isNotAir() {
    return !this.equals(BlockIngredient.AIR) && this.getAll().stream().noneMatch(state -> state.equals(PartialBlockState.AIR));
  }

  public boolean isNotAny() {
    return !this.equals(BlockIngredient.ANY) && this.getAll().stream().noneMatch(state -> state.equals(PartialBlockState.ANY));
  }

  public boolean isNotMachine() {
    return this.equals(BlockIngredient.NOT_MACHINE) && this.getAll().stream().allMatch(state -> state.equals(PartialBlockState.NOT_MACHINE));
  }

  public MutableComponent getNamesUnified() {
    MutableComponent name = Component.empty();
    MutableComponent last = Component.empty();
    Component current;
    Iterator<Component> iterator = getNames().iterator();
    if (getNames().size() > 1) {
      name.append("[");
      last.append("]");
    }
    while (iterator.hasNext()) {
      current = iterator.next();
      name.append(current);
      if (iterator.hasNext()) {
        name.append(", ");
      }
    }
    name.append(last);
    return name;
  }

  public String getString() {
    List<String> ingredients = Lists.newArrayList();
    ingredients.addAll(this.tags.stream().map(TagKey::location).map(ResourceLocation::toString).map(s -> "#" + s).toList());

    ingredients.addAll(
        uniqueStates()
            .map(PartialBlockState::toString)
            .toList()
    );

    if (ingredients.size() == 1) {
      return ingredients.get(0);
    }

    return ingredients.toString();
  }

  @Override
  public String toString() {
    return asJson().toString();
  }

  public BlockIngredient copyWithRotation(Rotation rotation) {
    return new BlockIngredient(tags, getAll().stream().map(state -> state.copyWithRotation(rotation)).toList());
  }

  public BlockIngredient merge(BlockIngredient other) {
    if (other == null) return AIR.merge(this);
    List<PartialBlockState> ingredients = Lists.newArrayList();
    ingredients.addAll(getAll());
    ingredients.addAll(other.getAll());
    List<TagKey<Block>> tags = Lists.newArrayList();
    tags.addAll(this.tags);
    tags.addAll(other.tags);
    return new BlockIngredient(tags, ingredients);
  }

  @Override
  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    json.addProperty("isTag", isTag);
    json.addProperty("tags", tags.toString());
    JsonArray array = new JsonArray();
    getAll().forEach(state -> array.add(state.toString()));
    json.add("states", array);
    return json;
  }

  public CompoundTag asTag() {
    CompoundTag tag = new CompoundTag();
    tag.putBoolean("isTag", isTag);
    ListTag tagList = new ListTag();
    tags.forEach(t -> tagList.add(StringTag.valueOf(t.toString())));
    tag.put("tags", tagList);
    ListTag states = new ListTag();
    getAll().forEach(state -> states.add(StringTag.valueOf(state.toString())));
    tag.put("states", states);
    return tag;
  }

  public static BlockIngredient of(Object o) {
    return (BlockIngredient) o;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BlockIngredient that)) return false;
    return isTag == that.isTag && Objects.equals(partialBlockStates, that.partialBlockStates) && Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partialBlockStates, isTag, tags);
  }
}
