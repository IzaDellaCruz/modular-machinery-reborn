package es.degrassi.mmreborn.common.registration;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.network.DataType;
import es.degrassi.mmreborn.api.network.IData;
import es.degrassi.mmreborn.api.network.data.BooleanData;
import es.degrassi.mmreborn.api.network.data.DoubleData;
import es.degrassi.mmreborn.api.network.data.FloatData;
import es.degrassi.mmreborn.api.network.data.FluidStackData;
import es.degrassi.mmreborn.api.network.data.IntegerData;
import es.degrassi.mmreborn.api.network.data.ItemStackData;
import es.degrassi.mmreborn.api.network.data.LongData;
import es.degrassi.mmreborn.api.network.data.NbtData;
import es.degrassi.mmreborn.api.network.data.StringData;
import es.degrassi.mmreborn.api.network.syncable.BooleanSyncable;
import es.degrassi.mmreborn.api.network.syncable.DoubleSyncable;
import es.degrassi.mmreborn.api.network.syncable.FloatSyncable;
import es.degrassi.mmreborn.api.network.syncable.FluidStackSyncable;
import es.degrassi.mmreborn.api.network.syncable.IntegerSyncable;
import es.degrassi.mmreborn.api.network.syncable.ItemStackSyncable;
import es.degrassi.mmreborn.api.network.syncable.LongSyncable;
import es.degrassi.mmreborn.api.network.syncable.NbtSyncable;
import es.degrassi.mmreborn.api.network.syncable.StringSyncable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static es.degrassi.mmreborn.ModularMachineryReborn.rootLC;

public class DataRegistration {
  private DataRegistration() {}
  public static final DeferredRegister<DataType<? extends IData<?>, ?>> DATAS =
      DeferredRegister.create(DataType.REGISTRY_KEY, ModularMachineryReborn.MODID);
  public static final Registry<DataType<? extends IData<?>, ?>> DATA_REGISTRY = DATAS.makeRegistry(builder -> {});

  public static final Supplier<DataType<BooleanData, Boolean>> BOOLEAN_DATA = DATAS.register(rootLC("boolean"),
      () -> DataType.create(Boolean.class, BooleanSyncable::create, BooleanData::new));
  public static final Supplier<DataType<IntegerData, Integer>> INTEGER_DATA = DATAS.register(rootLC("integer"),
      () -> DataType.create(Integer.class, IntegerSyncable::create, IntegerData::new));
  public static final Supplier<DataType<DoubleData, Double>> DOUBLE_DATA = DATAS.register(rootLC("double"),
      () -> DataType.create(Double.class, DoubleSyncable::create, DoubleData::new));
  public static final Supplier<DataType<FloatData, Float>> FLOAT_DATA = DATAS.register(rootLC("float"),
      () -> DataType.create(Float.class, FloatSyncable::create, FloatData::new));
  public static final Supplier<DataType<ItemStackData, ItemStack>> ITEMSTACK_DATA = DATAS.register(rootLC("itemstack"),
      () -> DataType.create(ItemStack.class, ItemStackSyncable::create, ItemStackData::new));
  public static final Supplier<DataType<FluidStackData, FluidStack>> FLUIDSTACK_DATA = DATAS.register(rootLC("fluidstack"),
      () -> DataType.create(FluidStack.class, FluidStackSyncable::create, FluidStackData::new));
  public static final Supplier<DataType<StringData, String>> STRING_DATA = DATAS.register(rootLC("string"),
      () -> DataType.create(String.class, StringSyncable::create, StringData::new));
  public static final Supplier<DataType<LongData, Long>> LONG_DATA = DATAS.register(rootLC("long"),
      () -> DataType.create(Long.class, LongSyncable::create, LongData::new));
  public static final Supplier<DataType<NbtData, CompoundTag>> NBT_DATA = DATAS.register(rootLC("nbt"),
      () -> DataType.create(CompoundTag.class, NbtSyncable::create, NbtData::new));


  public static void register(final IEventBus bus) {
    DATAS.register(bus);
  }
}
