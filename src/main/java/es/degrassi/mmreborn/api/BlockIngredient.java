package es.degrassi.mmreborn.api;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import es.degrassi.mmreborn.api.codec.NamedCodec;
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
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

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

public class BlockIngredient implements IIngredient<PartialBlockState, BlockInWorld> {
  public static final BlockIngredient AIR = new BlockIngredient(false, PartialBlockState.AIR);
  public static final BlockIngredient ANY = new BlockIngredient(false, PartialBlockState.ANY);
  public static final BlockIngredient MACHINE = new BlockIngredient(false, PartialBlockState.MACHINE);
  public static final BlockIngredient NOT_MACHINE = new BlockIngredient(true, PartialBlockState.MACHINE);

  public static final NamedCodec<BlockIngredient> STRING_CODEC = NamedCodec.STRING.comapFlatMap(s -> {
    try {
      final String original = s;
      StringReader reader = new StringReader(s);
      reader.skipWhitespace();
      boolean not = false;
      if (reader.peek() == '!') {
        not = true;
        reader.skip();
      }
      if (reader.peek() == '[') {
        reader.skip();
        if (reader.getRemaining().endsWith("]")) {
          s = reader.getRemaining().substring(0, s.length() - 1);
        } else {
          s = reader.getRemaining();
        }
      }
      String[] arr = s.split(", ");
      return DataResult.success(
          Arrays.stream(arr)
              .map(string -> {
                try {
                  return BlockIngredient.of(string);
                } catch (CommandSyntaxException e) {
                  throw new IllegalArgumentException(e);
                }
              })
              .reduce(new BlockIngredient(not, Collections.emptyList(), Collections.emptyList()), BlockIngredient::merge)
      );
    } catch(IllegalArgumentException e) {
      return DataResult.error(e::getMessage);
    }
  }, BlockIngredient::getString, "BlockIngredient from string");

  public static final NamedCodec<BlockIngredient> ING_CODEC = NamedCodec.either(
      PartialBlockState.CODEC,
      STRING_CODEC,
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

  public static final NamedCodec<BlockIngredient> MAP_CODEC = NamedCodec.record(blockIngredientInstance ->
          blockIngredientInstance.group(
              NamedCodec.BOOL.optionalFieldOf("not", false).forGetter(ingredient -> ingredient.not),
              ING_CODEC.fieldOf("ingredient").forGetter(Function.identity())
          ).apply(blockIngredientInstance, (not, ingredient) -> new BlockIngredient(not, ingredient.getTags(),
              ingredient.uniqueStates().toList())),
      "Block ingredient"
  );

  public static final NamedCodec<BlockIngredient> CODEC =
      NamedCodec.either(MAP_CODEC, STRING_CODEC).xmap(either -> either.map(Function.identity(), Function.identity()),
          Either::left, "Block Ingredient");

  private final Supplier<List<PartialBlockState>> partialBlockStates;
  @Getter
  @Setter
  private List<TagKey<Block>> tags = Lists.newArrayList();
  @Getter
  private final boolean not;

  public BlockIngredient(List<TagKey<Block>> tags, List<PartialBlockState> states) {
    this(false, tags, states);
  }

  public BlockIngredient(boolean not, List<TagKey<Block>> tags, List<PartialBlockState> states) {
    List<PartialBlockState> statesCopy = Lists.newArrayList(states);
    this.tags.addAll(tags);
    this.not = not;
    tags.forEach(tag ->
        statesCopy.addAll(TagUtil.getBlocks(tag)
            .map(PartialBlockState::new)
            .toList())
    );
    this.partialBlockStates = Suppliers.memoize(() -> ImmutableList.copyOf(statesCopy));
  }

  public BlockIngredient(boolean not, PartialBlockState partialBlockState) {
    this(not, Collections.emptyList(), Collections.singletonList(partialBlockState));
  }

  public BlockIngredient(PartialBlockState partialBlockState) {
    this(false, partialBlockState);
  }

  public static BlockIngredient create(Object o) throws IllegalArgumentException, CommandSyntaxException {
    if (o instanceof List<?> sa) {
      return sa.stream()
          .filter(s -> s instanceof CharSequence)
          .map(s -> (CharSequence) s)
          .map(s -> {
            try {
              return BlockIngredient.of(s);
            } catch (CommandSyntaxException e) {
              throw new IllegalArgumentException(e);
            }
          })
          .reduce(
              new BlockIngredient(false, Collections.emptyList(), Collections.emptyList()),
            (curr, prev) -> prev.merge(curr)
          );
    } else if (!(o instanceof CharSequence s)) throw new IllegalArgumentException("Block ingredient must be a string or string[]");
    else return BlockIngredient.of(s);
  }

  public BlockIngredient copy() {
    return new BlockIngredient(
        not,
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
  public boolean test(BlockInWorld block) {
    boolean isTag = !this.tags.isEmpty();
    if (isTag) {
      if (not) {
        return this.tags.stream().noneMatch(tag -> block.getState().is(tag));
      } else {
        return this.tags.stream().anyMatch(tag -> block.getState().is(tag));
      }
    } else {
      if (not) {
        return uniqueStates().noneMatch(state -> state.test(block));
      } else {
        return uniqueStates().anyMatch(state -> state.test(block));
      }
    }
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
      return ingredients.getFirst();
    }

    return (this.not ? "!" : "") + ingredients;
  }

  @Override
  public String toString() {
    return asJson().toString();
  }

  public BlockIngredient copyWithRotation(Rotation rotation) {
    return new BlockIngredient(not, tags, getAll().stream().map(state -> state.copyWithRotation(rotation)).toList());
  }

  public BlockIngredient merge(BlockIngredient other) {
    if (other == null) return AIR.merge(this);
    List<PartialBlockState> ingredients = Lists.newArrayList();
    ingredients.addAll(getAll());
    ingredients.addAll(other.getAll());
    List<TagKey<Block>> tags = Lists.newArrayList();
    tags.addAll(this.tags);
    tags.addAll(other.tags);
    return new BlockIngredient(other.not || not, tags, ingredients);
  }

  @Override
  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    json.addProperty("not", not);
    json.addProperty("tags", tags.toString());
    JsonArray array = new JsonArray();
    getAll().forEach(state -> array.add(state.toString()));
    json.add("states", array);
    return json;
  }

  public CompoundTag asTag() {
    CompoundTag tag = new CompoundTag();
    ListTag tagList = new ListTag();
    tag.putBoolean("not", not);
    tags.forEach(t -> tagList.add(StringTag.valueOf(t.toString())));
    tag.put("tags", tagList);
    ListTag states = new ListTag();
    getAll().forEach(state -> states.add(StringTag.valueOf(state.toString())));
    tag.put("states", states);
    return tag;
  }

  public static BlockIngredient of(CharSequence s) throws CommandSyntaxException {
    StringReader reader = new StringReader(s.toString());

    reader.skipWhitespace();

    boolean not = false;

    if (reader.peek() == '!') {
      not = true;
      reader.skip();
    }

    if (reader.peek() == '#') {
      reader.skip();
      TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(reader.getRemaining()));
      return new BlockIngredient(not, Collections.singletonList(tag), Collections.emptyList());
    }

    PartialBlockState state = PartialBlockState.of(reader.getRemaining());
    return new BlockIngredient(not, Collections.emptyList(), Collections.singletonList(state));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BlockIngredient that)) return false;
    return this.not == that.not && Objects.equals(partialBlockStates, that.partialBlockStates) && Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(not, partialBlockStates, tags);
  }
}
