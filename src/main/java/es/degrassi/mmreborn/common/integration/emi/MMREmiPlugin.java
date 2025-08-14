package es.degrassi.mmreborn.common.integration.emi;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.integration.almostunified.RecipeIndicator;
import es.degrassi.mmreborn.client.screen.ControllerScreen;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.integration.almostunified.AlmostUnifiedAdapter;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import es.degrassi.mmreborn.common.registration.RecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class MMREmiPlugin implements EmiPlugin {
  public static final Map<DynamicMachine, EmiRecipeCategory> categories = Maps.newHashMap();
  @Override
  public void register(EmiRegistry registry) {
    EmiStack controller = EmiStack.of(ItemRegistration.CONTROLLER);

    registry.setDefaultComparison(controller, Comparison.compareData(stack -> stack.get(DataComponentRegistration.MACHINE_DATA.get())));

    registry.addEmiStack(controller);

    RecipeManager manager = registry.getRecipeManager();
    List<RecipeHolder<MachineRecipe>> recipes = manager.getAllRecipesFor(RecipeRegistration.RECIPE_TYPE.get());

    ModularMachineryReborn.MACHINES.forEach((id, machine) -> {
      ItemStack is = new ItemStack(ItemRegistration.CONTROLLER.get());
      is.set(DataComponentRegistration.MACHINE_DATA.get(), id);
      EmiStack stack = EmiStack.of(is);
      EmiRecipeCategory category = new EmiRecipeCategory(id, stack) {
        @Override
        public Component getName() {
          return Component.literal(machine.getLocalizedName());
        }
      };
      categories.put(machine, category);
      registry.addCategory(category);
      registry.addWorkstation(category, EmiStack.of(ItemRegistration.BLUEPRINT.get()));
      registry.addWorkstation(category, stack);
      recipes.stream()
          .filter(recipe -> recipe.value().getOwningMachine() != null)
          .filter(recipe -> recipe.value().getOwningMachine().getRegistryName().equals(id))
          .filter(recipe -> !recipe.value().isHidden())
          .forEach(recipe -> registry.addDeferredRecipes(x -> x.accept(new MMREmiRecipe(category, recipe))));
      registry.addExclusionArea(ControllerScreen.class, (screen, consumer) -> {
        int x = screen.getGuiLeft(), y = screen.getGuiTop();
        int width = screen.xSize, height = screen.ySize;
        List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> sizes = screen.popups().stream()
            .filter(popup -> {
              boolean widths = popup.x >= x + width || popup.x + popup.xSize >= x + width || popup.x <= x;
              boolean heights = popup.y >= y + height || popup.y + popup.ySize >= y + height || popup.y <= y;
              return widths || heights;
            })
            .map(popup -> Pair.of(Pair.of(popup.x, popup.y), Pair.of(popup.xSize, popup.ySize))).toList();
        int minX = sizes.stream()
            .mapToInt(pair -> pair.getFirst().getFirst())
            .min()
            .orElse(x);
        int maxX = sizes.stream()
            .mapToInt(pair -> pair.getFirst().getFirst() + pair.getSecond().getFirst())
            .max()
            .orElse(x);
        int minY = sizes.stream()
            .mapToInt(pair -> pair.getFirst().getSecond())
            .min()
            .orElse(y);
        int maxY = sizes.stream()
            .mapToInt(pair -> pair.getFirst().getSecond() + pair.getSecond().getSecond())
            .max()
            .orElse(y);
        consumer.accept(new Bounds(minX, minY, maxX, maxY));
      });
      registry.addRecipeDecorator(category, new IndicatorDecorator());
      //registry.addRecipeHandler(ContainerRegistration.CONTROLLER.get(), new MMREmiRecipeHandler(machine));
    });

    registry.removeEmiStacks(stack -> {
      ResourceLocation machineId = stack.getItemStack().getComponents().get(DataComponentRegistration.MACHINE_DATA.get());
      return stack.isEqual(controller) && (machineId == null || machineId.toString().equals(ControllerItem.DUMMY.toString()));
    });
  }

  /**
   * This decorator is adapted from AlmostUnified  <a href="https://github.com/AlmostReliable/almostunified/blob/1.21.1/Common/src/main/java/com/almostreliable/unified/compat/viewer/AlmostEMI.java">AlmostEMI$IndicatorDecorator</a>
   */
  private static class IndicatorDecorator implements EmiRecipeDecorator {

    @Override
    public void decorateRecipe(EmiRecipe recipe, WidgetHolder widgets) {
      var recipeId = recipe.getId();
      if (recipeId == null) return;

      if (recipe instanceof MMREmiRecipe r) {
        int pX = r.getDisplayWidth() - 5;
        int pY = r.getDisplayHeight() - 3;
        int size = RecipeIndicator.RENDER_SIZE - 1;
        var link = r.getRecipe();
        if (!AlmostUnifiedAdapter.isRecipeModified(link)) return;

        widgets.addDrawable(0, 0, 0, 0, (guiGraphics, mX, mY, delta) ->
            RecipeIndicator.renderIndicator(guiGraphics, pX, pY, size));
        widgets.addTooltipText(RecipeIndicator.constructTooltip(link), pX, pY, size, size);
      }
    }
  }
}
