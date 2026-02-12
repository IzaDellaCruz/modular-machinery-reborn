package es.degrassi.mmreborn.common.integration.jade;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluid;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluidPerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.util.MMRLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.List;

public class DynamicMachineServerDataProvider implements IServerDataProvider<BlockAccessor> {

  public static final DynamicMachineServerDataProvider INSTANCE = new DynamicMachineServerDataProvider();
  public static final ResourceLocation ID = ModularMachineryReborn.rl("machine_server_data_provider");

  @Override
  public void appendServerData(CompoundTag nbt, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof MachineControllerEntity machine && machine.getLevel() != null) {
      CompoundTag tag = new CompoundTag();
      if (machine.isPaused()) {
        tag.putBoolean("paused", true);
      } else {
        tag.put("status", machine.getCraftingStatus().serializeNBT(accessor.getLevel().registryAccess()));
        var runningCores = machine.getProcessor()
            .cores()
            .stream()
            .filter(MachineProcessorCore::isActive)
            .filter(MachineProcessorCore::hasActiveRecipe)
            .count();
        tag.putLong("runningCores", runningCores);
        if (runningCores == 1) {
          ListTag itemOutputs = new ListTag();
          ListTag fluidOutputs = new ListTag();
          var core = machine.getProcessor()
              .cores()
              .stream()
              .filter(MachineProcessorCore::hasActiveRecipe)
              .findFirst();
          if (core.isEmpty()) return;
          var outRequirements = core
              .map(MachineProcessorCore::getCurrentRecipe)
              .map(RecipeHolder::value)
              .map(MachineRecipe::getRequirements)
              .stream()
              .flatMap(List::stream)
              .filter(r -> r.requirement().getMode().isOutput())
              .toList();
          outRequirements.stream()
              .filter(r -> r.requirement() instanceof RequirementItem)
              .forEach(r -> SizedIngredient.FLAT_CODEC.encodeStart(NbtOps.INSTANCE, ((RequirementItem) r.requirement()).getIngredient()).resultOrPartial().ifPresent(ing -> {
                CompoundTag req = new CompoundTag();
                req.put("ingredient", ing);
                req.putFloat("chance", r.chance());
                itemOutputs.add(req);
              }));
          outRequirements.stream()
                .filter(r -> r.requirement() instanceof RequirementFluid || r.requirement() instanceof RequirementFluidPerTick)
                .forEach(r -> {
                  CompoundTag req = new CompoundTag();
                  req.putFloat("chance", r.chance());
                  switch (r.requirement()) {
                    case RequirementFluid f -> SizedFluidIngredient.FLAT_CODEC.encodeStart(NbtOps.INSTANCE, f.getIngredient())
                        .resultOrPartial().ifPresent(ing -> {
                          req.put("ingredient", ing);
                          fluidOutputs.add(req);
                        });
                    case RequirementFluidPerTick f -> SizedFluidIngredient.FLAT_CODEC.encodeStart(NbtOps.INSTANCE, f.getIngredient())
                        .resultOrPartial().ifPresent(ing -> {
                          req.put("ingredient", ing);
                          fluidOutputs.add(req);
                        });
                    default -> {}
                  }
                });
          CompoundTag outputs = new CompoundTag();
          outputs.put("item", itemOutputs);
          outputs.put("fluid", fluidOutputs);
          tag.put("outputs", outputs);
          CompoundTag progress = new CompoundTag();
          progress.putFloat("current", core.get().getRecipeProgressTime());
          progress.putFloat("total", core.get().getRecipeTotalTime());
          tag.put("progress", progress);
        }
      }
      nbt.put(ModularMachineryReborn.MODID + ".controller", tag);
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ID;
  }
}
