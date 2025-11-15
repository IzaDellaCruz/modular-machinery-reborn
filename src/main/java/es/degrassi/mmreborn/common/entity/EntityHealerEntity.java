package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementEntity;
import es.degrassi.mmreborn.common.entity.base.EntityBaseEntity;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EntityHealerEntity extends EntityBaseEntity {
  public EntityHealerEntity(BlockPos pos, BlockState blockState) {
    super(EntityRegistration.ENTITY_HEALER.get(), pos, blockState, RequirementEntity.Action.ADD_HEALTH);
  }
}
