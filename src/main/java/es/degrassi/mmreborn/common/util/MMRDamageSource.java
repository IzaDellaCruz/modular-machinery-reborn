package es.degrassi.mmreborn.common.util;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.entity.base.EntityBaseEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class MMRDamageSource extends DamageSource {
  private String machineName;
  private final EntityBaseEntity delegate;
  private boolean fromDelegate = false;
  public MMRDamageSource(@Nullable MachineControllerEntity entity, EntityBaseEntity delegate) {
    super(
        delegate.getLevel()
            .registryAccess()
            .registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(
                ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ModularMachineryReborn.rl("modular_machinery_damage")
                )
            )
    );
    this.delegate = delegate;
    if (entity != null)
      this.machineName = entity.getFoundMachine().getLocalizedName();
  }

  @Override
  public Component getLocalizedDeathMessage(LivingEntity dead) {
    if (this.machineName == null || this.fromDelegate) {
      if (this.delegate.getController() != null) {
        this.machineName = this.delegate.getController().getFoundMachine().getLocalizedName();
        this.fromDelegate = false;
      } else {
        this.machineName = this.delegate.getBlockState().getBlock().getName().getString();
        this.fromDelegate = true;
      }
    }
    return Component.translatable("mmr.damagesource.kill", dead.getDisplayName(), this.machineName);
  }
}
