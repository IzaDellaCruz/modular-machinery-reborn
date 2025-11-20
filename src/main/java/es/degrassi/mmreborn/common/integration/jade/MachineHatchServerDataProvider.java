package es.degrassi.mmreborn.common.integration.jade;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.entity.EffectDispenserEntity;
import es.degrassi.mmreborn.common.entity.FuelTankEntity;
import es.degrassi.mmreborn.common.entity.RedstonePortEntity;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.util.RomanNumber;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class MachineHatchServerDataProvider implements IServerDataProvider<BlockAccessor> {
  public static final MachineHatchServerDataProvider INSTANCE = new MachineHatchServerDataProvider();
  public static final ResourceLocation ID = ModularMachineryReborn.rl("hatch_server_data_provider");
  @Override
  public void appendServerData(CompoundTag nbt, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof ColorableMachineComponentEntity entity && entity.getLevel() != null) {
      CompoundTag tag = new CompoundTag();
      if (entity instanceof FuelTankEntity fuelTank) {
        CompoundTag fuelTag = new CompoundTag();
        fuelTag.putLong("amount", fuelTank.getFuelHandler().getFuel());
        fuelTag.putLong("capacity", fuelTank.getFuelHandler().getMaxFuel());
        tag.put("fuel", fuelTag);
      }

      if (entity instanceof RedstonePortEntity redstoneEntity) {
        CompoundTag redstoneTag = new CompoundTag();
        redstoneTag.putBoolean("emit", redstoneEntity.getMode().isOutput());
        redstoneTag.putInt(
            "power",
            redstoneEntity.getMode().isOutput()
                ? redstoneEntity.getOutputAmount()
                : redstoneEntity.provideComponent().getContainerProvider()
        );
        tag.put("redstone", redstoneTag);
      }

      if (entity instanceof EffectDispenserEntity effectEntity) {
        CompoundTag effectTag = new CompoundTag();
        effectTag.putBoolean("giving", effectEntity.isApplyingEffect());
        effectTag.putInt("radius", effectEntity.getSize().radius);
        effectTag.putBoolean("interdimensional", effectEntity.getSize().interdimensional);
        effectEntity.getEffect().ifPresent(effect -> {
          effectTag.put("instance", effect.save());
          effectTag.putString("level", RomanNumber.toRoman(effect.getAmplifier() + 1));
        });
        tag.put("effect", effectTag);
      }

      if (entity instanceof IAutoEntity<?>) {
        CompoundTag autoTag = new CompoundTag();
        if (entity instanceof IAutoInputEntity auto) {
          autoTag.putBoolean("input", auto.isShouldAutoInput());
        }

        if (entity instanceof IAutoOutputEntity auto) {
          autoTag.putBoolean("output", auto.isShouldAutoOutput());
        }
        tag.put("auto", autoTag);
      }

      nbt.put(ModularMachineryReborn.MODID + ".hatch", tag);
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ID;
  }
}
