package es.degrassi.mmreborn.client.integration.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import es.degrassi.mmreborn.api.TagUtil;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.integration.emi.RegisterEmiComponentEvent;
import es.degrassi.mmreborn.api.integration.emi.RegisterEmiEmptyRequirementEvent;
import es.degrassi.mmreborn.api.integration.emi.RegisterEmiRequirementToIngredientEvent;
import es.degrassi.mmreborn.api.integration.emi.RegisterEmiRequirementToStackEvent;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDurability;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDurabilityPerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiBiomeComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiChunkloadComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiDimensionComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiDurabilityComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiDurabilityPerTickComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiEmptyComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiEnergyComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiEnergyPerTickComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiExperienceComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiExperiencePerTickComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiFluidComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiFluidPerTickComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiHeightComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiItemComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiLootTableComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiTimeComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiWeatherComponent;
import es.degrassi.mmreborn.common.integration.emi.EmiComponentRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiEmptyRequirementRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiIngredientRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiStackRegistry;
import es.degrassi.mmreborn.common.machine.component.DurabilityComponent;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.registration.EmptyRequirementTypeRegistration;
import es.degrassi.mmreborn.common.registration.RequirementTypeRegistration;
import es.degrassi.mmreborn.common.util.LootTableHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class MMREmiClientIntegration {
  public MMREmiClientIntegration(IEventBus bus) {
    bus.register(this);
    EmiComponentRegistry.init();
    EmiStackRegistry.init();
    EmiIngredientRegistry.init();
    EmiEmptyRequirementRegistry.init();
  }

  @SubscribeEvent
  public void registerEmiEmptyRequirement(final RegisterEmiEmptyRequirementEvent event) {
    event.register(
        EmptyRequirementTypeRegistration.ITEM.get(),
        (component, widgets, recipe) -> widgets.add(component)
    );
    event.register(
        EmptyRequirementTypeRegistration.FLUID.get(),
        (component, widgets, recipe) -> widgets.add(component)
    );
    event.register(
        EmptyRequirementTypeRegistration.ENERGY.get(),
        (component, widgets, recipe) -> widgets.add(component)
    );
  }

  @SubscribeEvent
  public void registerEmiComponents(final RegisterEmiComponentEvent event) {
    event.register(RequirementTypeRegistration.ENERGY.get(), EmiEnergyComponent::new);
    event.register(RequirementTypeRegistration.ENERGY_PER_TICK.get(), EmiEnergyPerTickComponent::new);
    event.register(RequirementTypeRegistration.EXPERIENCE.get(), EmiExperienceComponent::new);
    event.register(RequirementTypeRegistration.EXPERIENCE_PER_TICK.get(), EmiExperiencePerTickComponent::new);
    event.register(RequirementTypeRegistration.ITEM.get(), EmiItemComponent::new);
    event.register(RequirementTypeRegistration.DURABILITY.get(), EmiDurabilityComponent::new);
    event.register(RequirementTypeRegistration.DURABILITY_PER_TICK.get(), EmiDurabilityPerTickComponent::new);
    event.register(RequirementTypeRegistration.FLUID.get(), EmiFluidComponent::new);
    event.register(RequirementTypeRegistration.FLUID_PER_TICK.get(), EmiFluidPerTickComponent::new);
    event.register(RequirementTypeRegistration.BIOME.get(), EmiBiomeComponent::new);
    event.register(RequirementTypeRegistration.TIME.get(), EmiTimeComponent::new);
    event.register(RequirementTypeRegistration.HEIGHT.get(), EmiHeightComponent::new);
    event.register(RequirementTypeRegistration.CHUNKLOAD.get(), EmiChunkloadComponent::new);
    event.register(RequirementTypeRegistration.DIMENSION.get(), EmiDimensionComponent::new);
    event.register(RequirementTypeRegistration.WEATHER.get(), EmiWeatherComponent::new);
    event.register(RequirementTypeRegistration.LOOT_TABLE.get(), EmiLootTableComponent::new);
    event.register(RequirementTypeRegistration.EMPTY.get(), EmiEmptyComponent::new);
  }

  @SubscribeEvent
  public void registerEmiStacks(final RegisterEmiRequirementToStackEvent event) {
    event.register(
        RequirementTypeRegistration.ITEM.get(),
        this::emiStackFromItemRequirement
    );
    event.register(
        RequirementTypeRegistration.DURABILITY.get(),
        this::emiStackFromDurabilityRequirement
    );
    event.register(
        RequirementTypeRegistration.DURABILITY_PER_TICK.get(),
        this::emiStackFromDurabilityPerTickRequirement
    );
    event.register(
        RequirementTypeRegistration.FLUID.get(),
        requirement ->
            Arrays.stream(requirement.requirement().getIngredient().ingredient().getStacks())
                .map(stack -> EmiStack.of(stack.getFluid(), requirement.requirement().getIngredient().amount()))
                .toList()

    );
    event.register(
        RequirementTypeRegistration.FLUID_PER_TICK.get(),
        requirement ->
            Arrays.stream(requirement.requirement().getIngredient().ingredient().getStacks())
            .map(stack -> EmiStack.of(stack.getFluid(), requirement.requirement().getIngredient().amount()))
            .toList()
    );
    event.register(
        RequirementTypeRegistration.LOOT_TABLE.get(),
        requirement -> LootTableHelper
            .getLootsForTable(requirement.requirement().getLootTable())
            .stream()
            .map(LootTableHelper.LootData::stack)
            .map(EmiStack::of)
            .toList()
    );
  }

  @SubscribeEvent
  public void registerEmiIngredients(final RegisterEmiRequirementToIngredientEvent event) {
    event.register(
        RequirementTypeRegistration.ITEM.get(),
        this::emiIngredientFromItemRequirement
    );
    event.register(
        RequirementTypeRegistration.DURABILITY.get(),
        this::emiIngredientFromDurabilityRequirement
    );
    event.register(
        RequirementTypeRegistration.DURABILITY_PER_TICK.get(),
        this::emiIngredientFromDurabilityPerTickRequirement
    );
  }

  private EmiIngredient emiIngredientFromItemRequirement(RecipeRequirement<ItemComponent, RequirementItem> requirement) {
    return EmiIngredient.of(requirement.requirement().ingredient.ingredient(), requirement.requirement().ingredient.count());
  }

  private EmiIngredient emiIngredientFromDurabilityRequirement(RecipeRequirement<DurabilityComponent, RequirementDurability> requirement) {
    return EmiIngredient.of(ingredientFromDurabilityRequirement(requirement.requirement().ingredient), requirement.requirement().getAmount());
  }

  private EmiIngredient emiIngredientFromDurabilityPerTickRequirement(RecipeRequirement<DurabilityComponent,
      RequirementDurabilityPerTick> requirement) {
    return EmiIngredient.of(ingredientFromDurabilityRequirement(requirement.requirement().ingredient), requirement.requirement().getAmount());
  }

  private Ingredient ingredientFromDurabilityRequirement(Ingredient original) {
    List<ItemStack> items = Arrays.stream(original.getItems())
        .map(this::generateWithDurability)
        .flatMap(List::stream)
        .unordered()
        .toList();
    return Ingredient.of(items.stream());
  }

  private List<EmiStack> emiStackFromItemRequirement(RecipeRequirement<ItemComponent, RequirementItem> requirement) {
    List<EmiStack> stacks = Lists.newArrayList();
    for (Ingredient.Value value : requirement.requirement().getIngredient().ingredient().values) {
      if (value instanceof Ingredient.TagValue(TagKey<Item> tag)) {
        for (Item stack : TagUtil.getItems(tag).toList()) {
          stacks.add(EmiStack.of(new ItemStack(stack, requirement.requirement().ingredient.count()), requirement.requirement().ingredient.count()));
        }
      } else if (value instanceof Ingredient.ItemValue(ItemStack item)) {
        stacks.add(EmiStack.of(item.copyWithCount(requirement.requirement().ingredient.count()), requirement.requirement().ingredient.count()));
      }
    }
    return stacks;
  }

  private List<EmiStack> emiStackFromDurabilityRequirement(RecipeRequirement<DurabilityComponent, RequirementDurability> requirement) {
    List<EmiStack> stacks = Lists.newArrayList();
    for (Ingredient.Value value : requirement.requirement().getIngredient().values) {
      if (value instanceof Ingredient.TagValue(TagKey<Item> tag)) {
        for (Item stack : TagUtil.getItems(tag).toList()) {
          stacks.addAll(generateWithDurability(new ItemStack(stack), requirement.requirement().getAmount()));
        }
      } else if (value instanceof Ingredient.ItemValue(ItemStack item)) {
        stacks.addAll(generateWithDurability(item, requirement.requirement().getAmount()));
      }
    }
    return stacks.stream().unordered().toList();
  }

  private List<EmiStack> emiStackFromDurabilityPerTickRequirement(RecipeRequirement<DurabilityComponent,
      RequirementDurabilityPerTick> requirement) {
    List<EmiStack> stacks = Lists.newArrayList();
    for (Ingredient.Value value : requirement.requirement().getIngredient().values) {
      if (value instanceof Ingredient.TagValue(TagKey<Item> tag)) {
        for (Item stack : TagUtil.getItems(tag).toList()) {
          stacks.addAll(generateWithDurability(new ItemStack(stack), requirement.requirement().getAmount()));
        }
      } else if (value instanceof Ingredient.ItemValue(ItemStack item)) {
        stacks.addAll(generateWithDurability(item, requirement.requirement().getAmount()));
      }
    }
    return stacks.stream().unordered().toList();
  }

  private List<ItemStack> generateWithDurability(ItemStack stack) {
    if (!stack.isDamageableItem()) throw new IllegalArgumentException(
        String.format("Invalid Item given in durability requirement, is not damageable: %s", stack.getDisplayName().getString())
    );
    int maxDamage = stack.getMaxDamage();
    List<ItemStack> damagedItems = Lists.newArrayList();
    for (int i = 0; i <= maxDamage; i++) {
      ItemStack copy = stack.copy();
      copy.setDamageValue(i);
      damagedItems.add(copy);
    }
    return damagedItems.stream().unordered().toList();
  }

  private List<EmiStack> generateWithDurability(ItemStack stack, int amount) {
    return generateWithDurability(stack).stream().map(s -> EmiStack.of(s, amount)).toList();
  }
}
