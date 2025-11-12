package es.degrassi.mmreborn.api.capability;

import es.degrassi.mmreborn.common.block.prop.EffectDispenserSize;
import es.degrassi.mmreborn.common.entity.EffectDispenserEntity;
import es.degrassi.mmreborn.common.network.server.component.SUpdateEffectComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EffectHandler implements INBTSerializable<CompoundTag> {
  private static final String SIZE_TAG = "size";
  private static final String EFFECT_TAG = "effect";
  private static final String IS_APPLYING_TAG = "isApplying";

  private EffectDispenserSize size;
  private final EffectDispenserEntity entity;
  @Getter
  @Setter
  private boolean isApplyingEffect = false;
  @Getter
  @Setter
  private MobEffectInstance effect;
  public EffectHandler(EffectDispenserSize size, EffectDispenserEntity entity) {
    this.size = size;
    this.entity = entity;
  }

  public void setData(EffectDispenserSize size, Optional<MobEffectInstance> effect) {
    this.size = size;
    effect.ifPresentOrElse(ef -> {
      this.effect = ef;
      this.isApplyingEffect = true;
    }, () -> {
      this.isApplyingEffect = false;
      this.effect = null;
    });
  }

  public void applyEffect(MobEffectInstance effect, Predicate<Entity> filter) {
    this.setData(size, Optional.of(effect));
    if (size.interdimensional) {
      Stream.Builder<ServerLevel> levels = Stream.builder();
      entity.getLevel().getServer().getAllLevels().forEach(levels::add);
      levels
          .build()
          .map(level -> {
            var worldBorder = level.getWorldBorder();
            AABB worldBB = new AABB(worldBorder.getMinX(), level.getMinBuildHeight(), worldBorder.getMinZ(), worldBorder.getMaxX(), level.getMaxBuildHeight(), worldBorder.getMaxZ());
            return level.getEntitiesOfClass(LivingEntity.class, worldBB, filter);
          })
          .flatMap(List::stream)
          .forEach(entity -> entity.addEffect(effect));
      setChanged();
      return;
    }
    BlockPos machinePos = entity.getBlockPos();
    AABB bb = new AABB(machinePos).inflate(size.radius);
    entity.getLevel().getEntitiesOfClass(LivingEntity.class, bb, filter).stream()
        .filter(entity -> entity.distanceToSqr(machinePos.getX(), machinePos.getY(), machinePos.getZ()) < size.radius * size.radius)
        .forEach(entity -> entity.addEffect(effect));
    setChanged();
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();
    nbt.putString(SIZE_TAG, size.getSerializedName());
    nbt.putBoolean(IS_APPLYING_TAG, isApplyingEffect);
    if (effect != null && !entity.getLevel().isClientSide)
      nbt.put(EFFECT_TAG, MobEffectInstance.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), effect).getOrThrow());
    return nbt;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
    this.size = EffectDispenserSize.value(nbt.getString(SIZE_TAG));
    this.isApplyingEffect = nbt.getBoolean(IS_APPLYING_TAG);
    if (nbt.contains(EFFECT_TAG))
      this.effect = loadEffect(nbt.getCompound(EFFECT_TAG), provider);
    else effect = null;
  }

  public void resetEffect() {
    setData(size, Optional.empty());
    setChanged();
  }

  public void setChanged() {
    entity.setChanged();
    if (entity.getLevel() instanceof ServerLevel sl) {
      PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(entity.getBlockPos()),
          new SUpdateEffectComponent(size, Optional.ofNullable(effect), entity.getBlockPos()));
    }
  }

  private static MobEffectInstance loadEffect(CompoundTag nbt, HolderLookup.Provider provider) {
    return MobEffectInstance.CODEC
        .parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt)
        .resultOrPartial()
        .orElse(null);
  }
}
