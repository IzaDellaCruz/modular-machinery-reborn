package es.degrassi.mmreborn.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.event.EventExit;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.level.CachedLevelBlock;
import dev.latvian.mods.rhino.Context;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class FunctionKubeEvent implements KubeEvent {

  private final ICraftingContext internal;
  @Getter
  private final MachineControllerJS machine;
  private final List<String> args;

  public FunctionKubeEvent(ICraftingContext internal, List<String> args) {
    this.internal = internal;
    this.machine = new MachineControllerJS(internal.getMachineTile());
    this.args = args;
  }

  public String get(int index) {
    if (index < 0 || index >= args.size())
      throw new IllegalArgumentException(String.format("Args index can not be less than 0 or greater than %s", args.size() - 1));
    return args.get(index);
  }

  public FunctionKubeEvent getContext() {
    return this;
  }

  public FunctionKubeEvent getCtx() {
    return this;
  }

  public float getRemainingTime() {
    return this.internal.getRemainingTime();
  }

  public float getBaseSpeed() {
    return this.internal.getBaseSpeed();
  }

  public void setBaseSpeed(float baseSpeed) {
    this.internal.setBaseSpeed(baseSpeed);
  }

  public float getModifiedSpeed() {
    return this.internal.getModifiedSpeed();
  }

  public MachineControllerEntity getTile() {
    return this.internal.getMachineTile();
  }

  public CachedLevelBlock getBlock() {
    return new CachedLevelBlock(getTile().getLevel(), getTile().getBlockPos());
  }

  public MachineRecipe getRecipe() {
    return internal.getRecipe();
  }

  public ResourceLocation getRecipeId() {
    return internal.getRecipeId();
  }

  @Override
  public Object defaultExitValue(Context cx) {
    return CraftingResult.pass();
  }

  public void error(Context cx, Component error) throws EventExit {
    this.cancel(cx, error);
  }
}
