package es.degrassi.mmreborn.common.crafting.requirement.emi;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

@Getter
public abstract class EmiComponent<X, R extends RecipeRequirement<?, ?, ?>> extends DrawableWidget implements IEmiRequirement<R> {
  protected static final ResourceLocation LOCATION_ICONS = ResourceLocation.fromNamespaceAndPath(ModularMachineryReborn.MODID, "textures/gui/jeirecipeicons.png");
  protected R requirement;
  protected final int uOffset, vOffset;
  private final boolean renderOverlay;

  protected EmiComponent(R requirement, int uOffset, int vOffset) {
    this(requirement, uOffset, vOffset, true);
  }

  protected EmiComponent(R requirement, int uOffset, int vOffset, boolean renderOverlay) {
    super(requirement.requirement().getPosition().x(), requirement.requirement().getPosition().y(), 0, 0, null);
    this.requirement = requirement;
    this.uOffset = uOffset;
    this.vOffset = vOffset;
    this.renderOverlay = renderOverlay;
  }

  @Override
  public Bounds getBounds() {
    return new Bounds(x, y, getWidth(), getHeight());
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public abstract int getWidth();

  public abstract int getHeight();

  @Nullable
  public ResourceLocation texture() {
    return LOCATION_ICONS;
  }

  @Override
  public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    EmiDrawContext context = EmiDrawContext.wrap(guiGraphics);
    context.push();
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    context.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    context.matrices().translate(x, y, 0);
    render(guiGraphics, mouseX, mouseY);
    context.pop();
    renderOverlay(guiGraphics, mouseX, mouseY);
  }

  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    ResourceLocation texture = texture();
    if (texture != null) {
      guiGraphics.blit(texture, 0, 0, 0, uOffset, vOffset, getWidth(), getHeight(),
          TextureSizeHelper.getWidth(texture()), TextureSizeHelper.getHeight(texture()));
    }
  }

  public final void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    if (shouldDrawSlotHighlight(mouseX, mouseY)) {
      drawSlotHighlight(guiGraphics);
    }
  }

  public final boolean shouldDrawSlotHighlight(int mouseX, int mouseY) {
    return renderOverlay && mouseX >= x && mouseX < x + getWidth() && mouseY >= y && mouseY < y + getHeight();
  }

  public int getXHighlight() {
    return 1;
  }

  public int getYHighlight() {
    return 1;
  }

  public final void drawSlotHighlight(GuiGraphics guiGraphics) {
    EmiDrawContext context = EmiDrawContext.wrap(guiGraphics);
    context.push();
    context.matrices().translate(x, y, 200);
    RenderSystem.disableDepthTest();
    RenderSystem.colorMask(true, true, true, false);
    context.fill(getXHighlight(), getYHighlight(), getWidth(), getHeight(), -2130706433);
    RenderSystem.colorMask(true, true, true, true);
    RenderSystem.enableDepthTest();
    context.pop();
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
    return new LinkedList<>();
  }

  public abstract List<X> ingredients();

  @Override
  public void addWidgets(WidgetHolder widgets, MMREmiRecipe recipe) {
    if (this instanceof RecipeHolder holder && !this.requirement.requirement().getMode().isInput()) holder.recipeContext(recipe);
    widgets.addTooltip(this::getTooltip, x, y, getWidth(), getHeight());
    widgets.add(this);
  }
}
