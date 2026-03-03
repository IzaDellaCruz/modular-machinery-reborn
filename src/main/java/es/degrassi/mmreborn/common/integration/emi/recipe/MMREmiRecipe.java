package es.degrassi.mmreborn.common.integration.emi.recipe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.crafting.requirement.DisplayInfoTemplate;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RequirementDisplayInfo;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDuration;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiDurationComponent;
import es.degrassi.mmreborn.common.crafting.requirement.emi.SlotTooltip;
import es.degrassi.mmreborn.common.integration.emi.EmiComponentRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiIngredientRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiStackRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MMREmiRecipe extends BasicEmiRecipe {
  protected static final int ICON_SIZE = 10;
  @Getter
  private final MachineRecipe recipe;
  public final int initialX = 8, gap = 8;
  @Getter
  protected int width = 256, height = 256;

  public final List<FormattedText> textsToRender = Lists.newArrayList();
  protected final LoadingCache<RecipeRequirement<?, ?, ?>, RequirementDisplayInfo> infoCache;
  protected boolean hasInfoRow;
  protected int rowY;
  protected int maxIconPerRow;

  public MMREmiRecipe(EmiRecipeCategory category, RecipeHolder<MachineRecipe> recipe) {
    super(category, recipe.id(), recipe.value().getWidth(), recipe.value().getHeight());
    this.recipe = recipe.value();
    this.inputs = this.recipe
        .getRequirements()
        .stream()
        .filter(requirement -> requirement.requirement().getMode().isInput())
        .filter(requirement -> EmiIngredientRegistry.hasEmiIngredient(requirement.getType()))
        .map(EmiIngredientRegistry::create)
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    this.outputs = this.recipe
        .getRequirements()
        .stream()
        .filter(requirement -> requirement.requirement().getMode().isOutput())
        .filter(requirement -> EmiStackRegistry.hasEmiStack(requirement.getType()))
        .map(EmiStackRegistry::create)
        .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    this.catalysts = this.recipe
        .getRequirements()
        .stream()
        .filter(requirement -> requirement.requirement().getMode().isNone())
        .filter(requirement -> EmiStackRegistry.hasEmiStack(requirement.getType()))
        .map(EmiStackRegistry::create)
        .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    this.infoCache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
      @Override
      public RequirementDisplayInfo load(RecipeRequirement<?, ?, ?> requirement) {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        requirement.getDisplayInfo(info);
        DisplayInfoTemplate template = requirement.info;
        if(template != null) {
          if(!template.getTooltips().isEmpty())
            info.getTooltips().clear();
          template.build(info);
        }
        return info;
      }
    });
    this.width = this.recipe.getWidth();
    this.height = this.recipe.getHeight();
    setupRecipeDimensions();
  }

  @Override
  public int getDisplayWidth() {
    return width;
  }

  @Override
  public int getDisplayHeight() {
    return height;
  }

  private void setupRecipeDimensions() {
    this.maxIconPerRow = this.width / (ICON_SIZE + 2);
    long maxDisplayRequirement = recipe
        .getDisplayInfoRequirements()
        .stream()
        .map(this.infoCache)
        .filter(RequirementDisplayInfo::shouldRender)
        .count();
    this.hasInfoRow = maxDisplayRequirement != 0;
    this.rowY = this.height;
    int rows = this.hasInfoRow ? Math.toIntExact(maxDisplayRequirement) / this.maxIconPerRow + 1 : 0;
    this.height = this.rowY + (ICON_SIZE + 2) * rows;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    textsToRender.clear();
    if (recipe.isShouldRenderProgress()) {
      new EmiDurationComponent(
          new RecipeRequirement<>(
              new RequirementDuration(recipe.getRecipeTotalTickTime(), recipe.getProgressData().position())
          ),
          1000,
          recipe.getProgressData(),
          false
      ).addWidgets(widgets, this);
    }
    Font font = Minecraft.getInstance().font;

    (recipe.getJeiRequirements().isEmpty() ? recipe.getRequirements() : recipe.getJeiRequirements())
        .stream()
        .filter(component -> EmiComponentRegistry.hasEmiComponent(component.getType()))
        .map(EmiComponentRegistry::create)
        .forEach(requirement -> requirement.addWidgets(widgets, this));

    Language language = Language.getInstance();
    AtomicInteger nextHeight = new AtomicInteger(0);
    AtomicInteger toRemove = new AtomicInteger(0);

    textsToRender.forEach(component -> {
      nextHeight.set(recipe.getHeight() - gap - font.wordWrapHeight(component, recipe.getWidth() - 8) - toRemove.get());
      widgets.addText(language.getVisualOrder(component), initialX, nextHeight.get(), 0xFF000000, false);
      toRemove.getAndAdd(font.wordWrapHeight(component, recipe.getWidth() - 8) + 2);
    });

    //Render the line between the gui elements and the requirements icons
    if(this.hasInfoRow) {
      widgets.add(new LineWidget());
    }
    //If no recipes have display infos stop here
    if(!this.hasInfoRow)
      return;

    AtomicInteger index = new AtomicInteger();
    AtomicInteger row = new AtomicInteger(0);
    recipe.getDisplayInfoRequirements().stream().map(this.infoCache).filter(RequirementDisplayInfo::shouldRender).forEach(info -> {
      int x = index.get() * (ICON_SIZE + 2) - 2;
      int y = this.rowY + 2 + (ICON_SIZE + 2) * row.get();
      if(index.incrementAndGet() >= this.maxIconPerRow) {
        index.set(0);
        row.incrementAndGet();
      }
      DisplayInfoWidget widget = new DisplayInfoWidget(x, y, info, recipe);
      widgets.add(widget);
    });
  }


  private class LineWidget extends Widget {
    @Override
    public Bounds getBounds() {
      return new Bounds(-3, rowY, width + 3, 1);
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
      draw.fill(-3, rowY, width + 3, rowY + 1, 0x30000000);
    }
  }

  private static class DisplayInfoWidget extends DrawableWidget {
    private final ScreenPosition pos;
    private final ScreenRectangle area;
    private final RequirementDisplayInfo info;
    private final MachineRecipe recipe;

    public DisplayInfoWidget(int x, int y, RequirementDisplayInfo info, MachineRecipe recipe) {
      super(x, y, ICON_SIZE, ICON_SIZE, null);
      this.pos = new ScreenPosition(x, y);
      this.area = new ScreenRectangle(x, y, ICON_SIZE, ICON_SIZE);
      this.info = info;
      this.recipe = recipe;
    }

    @Override
    public Bounds getBounds() {
      return new Bounds(pos.x(), pos.y(), area.width(), area.height());
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
      draw.pose().pushPose();
      draw.pose().translate(pos.x(), pos.y(), 0);
      this.info.renderIcon(draw, ICON_SIZE);
      draw.pose().popPose();
    }

    @Override
    public final List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
      List<ClientTooltipComponent> list = new LinkedList<>(getTooltip().stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList());
      if (this instanceof SlotTooltip tooltip) {
        tooltip.addSlotTooltip(list);
      }
      return list;
    }

    public List<Component> getTooltip() {
      return this.info.getTooltips().stream()
          .filter(pair -> pair.getSecond().shouldDisplay(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips))
          .map(Pair::getFirst)
          .toList();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
      if (Minecraft.getInstance().screen == null)
        return false;
      return this.info.handleClick(recipe.getOwningMachine(), this.recipe, button);
    }
  }
}
