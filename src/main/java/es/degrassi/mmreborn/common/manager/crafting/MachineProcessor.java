package es.degrassi.mmreborn.common.manager.crafting;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.crafting.IProcessor;
import es.degrassi.mmreborn.api.crafting.IProcessorTemplate;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.data.config.ParallelHatchConfig;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.registration.ProcessorTypeRegistration;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Accessors(fluent = true, makeFinal = true)
public class MachineProcessor implements IProcessor, ISyncableStuff {
  @Getter
  private final MachineControllerEntity tile;
  private boolean initialized = false;
  @Getter
  private final List<MachineProcessorCore> cores;

  public MachineProcessor(MachineControllerEntity tile) {
    this.tile = tile;
    int maxParallel = Math.max(
        MMRConfig.get().maxParallel.get(),
        ParallelHatchConfig.get().getMaxParallel()
    );
    cores = Lists.newArrayList();
    for (int i = 0; i < maxParallel; ++i) {
      cores.add(new MachineProcessorCore(this, tile, i + 1));
    }
  }

  public int getMaxCores() {
    AtomicInteger maxCores = new AtomicInteger(1);
    tile.getComponentManager().getParallel().ifPresent(pos -> {
      maxCores.set(pos.getMaxCores());
    });

    return Math.min(cores.size(), maxCores.get());
  }

  @Override
  public ProcessorType<MachineProcessor> getType() {
    return ProcessorTypeRegistration.MACHINE_PROCESSOR.get();
  }

  @Override
  public void tick() {
    if (!this.initialized)
      this.init();

    this.cores.forEach(MachineProcessorCore::tick);

    if (this.tile.getStatus() != MachineStatus.IDLE && this.cores.stream().noneMatch(MachineProcessorCore::hasActiveRecipe) && !this.tile.getStatus().isMissingStructure()) {
      this.tile.setStatus(MachineStatus.IDLE);
    }
  }

  public void updateActiveCores(int cores) {
    this.cores.forEach(core -> core.setActive(false));
    for (int i = 0; i < Math.min(cores, this.cores.size()); ++i) {
      this.cores.get(i).setActive(true);
    }
  }

  private void init() {
    this.initialized = true;
    AtomicInteger cores = new AtomicInteger(1);
    tile.getComponentManager().getParallel().ifPresent(pos -> cores.set(pos.getContainerProvider()));
    updateActiveCores(cores.get());
    this.cores.forEach(MachineProcessorCore::init);
  }

  public void setRunning() {
    this.tile.setStatus(MachineStatus.RUNNING);
  }

  public void setError(Component message) {
    if(this.cores.stream().allMatch(core -> !core.isActive() || core.getError() != null || core.getCurrentRecipe() == null))
      this.tile.setStatus(MachineStatus.ERRORED, message);
  }

  @Override
  public void reset() {
    this.cores.forEach(MachineProcessorCore::reset);
    if (this.tile.getStatus().isMissingStructure())
      this.tile.setStatus(this.tile.getStatus());
    else
      this.tile.setStatus(MachineStatus.IDLE);
  }

  @Override
  public void setMachineInventoryChanged() {
    if (!this.tile.getStatus().isCrafting())
      this.tile.setStatus(MachineStatus.IDLE);
    this.cores.forEach(MachineProcessorCore::setComponentChanged);
  }

  @Override
  public void setSearchImmediately() {
    if (!this.tile.getStatus().isCrafting())
      this.tile.setStatus(MachineStatus.IDLE);
    this.cores.forEach(MachineProcessorCore::setSearchImmediately);
  }

  @Override
  public CompoundTag serialize() {
    CompoundTag nbt = new CompoundTag();
    ListTag cores = new ListTag();
    this.cores.forEach(core -> cores.add(core.serialize()));
    nbt.put("cores", cores);
    return nbt;
  }

  @Override
  public void deserialize(CompoundTag nbt) {
    if(nbt.contains("cores", Tag.TAG_LIST)) {
      ListTag cores = nbt.getList("cores", Tag.TAG_COMPOUND);
      if(this.cores.size() == cores.size()) {
        for(int i = 0; i < this.cores.size(); i++)
          this.cores.get(i).deserialize(cores.getCompound(i));
      }
    }
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    this.cores.forEach(core -> core.getStuffToSync(container));
  }

  public record Template() implements IProcessorTemplate<MachineProcessor> {
    public static final Template DEFAULT = new Template();
    public static final NamedCodec<Template> CODEC = NamedCodec.unit(DEFAULT);

    @Override
    public ProcessorType<MachineProcessor> getType() {
      return ProcessorTypeRegistration.MACHINE_PROCESSOR.get();
    }

    @Override
    public MachineProcessor build(MachineControllerEntity tile) {
      return new MachineProcessor(tile);
    }
  }
}
