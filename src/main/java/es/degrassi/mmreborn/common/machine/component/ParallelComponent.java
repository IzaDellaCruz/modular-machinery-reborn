package es.degrassi.mmreborn.common.machine.component;

import com.google.gson.JsonObject;
import es.degrassi.mmreborn.common.block.prop.ParallelHatchSize;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.network.server.component.SUpdateCoresPacket;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ParallelComponent extends MachineComponent<Integer> {
  private int cores;
  private final ParallelHatchSize parallel;

  public ParallelComponent(ParallelHatchSize parallel) {
    super(IOType.INPUT);
    this.parallel = parallel;
    this.cores = 1;
  }

  @Override
  public ComponentType<Integer> getComponentType() {
    return ComponentRegistration.COMPONENT_PARALLEL.get();
  }

  @Override
  public Integer getContainerProvider() {
    return cores;
  }

  public Integer getMaxCores() {
    return parallel.max;
  }

  public void setCores(int cores, Level level, BlockPos pos) {
    this.cores = Math.min(cores, parallel.max);
    if (level instanceof ServerLevel sl)
      PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos), new SUpdateCoresPacket(cores, pos));
  }

  @Override
  public CompoundTag asTag(HolderLookup.Provider provider) {
    CompoundTag tag = super.asTag(provider);
    tag.putInt("cores", cores);
    tag.putInt("maxCores", parallel.max);
    return tag;
  }

  @Override
  public JsonObject asJson() {
    JsonObject json = super.asJson();
    json.addProperty("cores", cores);
    json.addProperty("maxCores", parallel.max);
    return json;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<Integer>> C merge(C c) {
    return (C) this;
  }
}
