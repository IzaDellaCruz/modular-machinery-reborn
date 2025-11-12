package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.api.capability.EffectHandler;
import es.degrassi.mmreborn.common.block.prop.EffectDispenserSize;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.entity.EffectDispenserEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class EffectComponent extends MachineComponent<EffectHandler> {
  private final EffectHandler handler;
  public EffectComponent(EffectHandler handler) {
    super(IOType.NONE);
    this.handler = handler;
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_EFFECT.get();
  }

  @Override
  public @Nullable EffectHandler getContainerProvider() {
    return handler;
  }

  @Override
  public <C extends MachineComponent<?>> C merge(C c) {
    return (C) this;
  }
}
