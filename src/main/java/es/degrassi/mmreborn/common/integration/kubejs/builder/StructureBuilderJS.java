package es.degrassi.mmreborn.common.integration.kubejs.builder;

import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;

import java.util.List;
import java.util.Map;

public class StructureBuilderJS {
  private final Structure.Builder builder;
  private List<List<String>> pattern;
  private Map<Character, BlockIngredient> keys;

  public static StructureBuilderJS create() {
    return new StructureBuilderJS(false);
  }
  public static StructureBuilderJS createRequirement() {
    return new StructureBuilderJS(true);
  }

  @HideFromJS
  private StructureBuilderJS(boolean requirement) {
    if (requirement) {
      builder = Structure.Builder.start('$');
    } else {
      builder = Structure.Builder.start('m');
    }
  }

  public StructureBuilderJS pattern(List<List<String>> pattern) {
    this.pattern = pattern;
    return this;
  }

  public StructureBuilderJS keys(Map<Character, BlockIngredient> keys) {
    this.keys = keys;
    return this;
  }

  public Structure build(List<ModifierReplacement> modifiers) {
    for (List<String> levels : pattern)
      builder.aisle(levels.toArray(new String[0]));
    for (Map.Entry<Character, BlockIngredient> key : keys.entrySet())
      builder.where(key.getKey(), key.getValue());
    return builder.build(pattern, keys, modifiers);
  }

  public Structure build() {
    return build(List.of());
  }
}
