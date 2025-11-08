package es.degrassi.mmreborn.client.integration.athena.model.controller;

import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import es.degrassi.mmreborn.client.integration.athena.utils.MMRAthenaQuad;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.util.MachineModelLocation;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ControllerData(
    DynamicMachine machine,
    @Nullable NullableEnumMap<Direction, Map<Direction, List<MMRAthenaQuad>>> data
) {
  public boolean hasCustomModel() {
    return modelLocation() != MachineModelLocation.DEFAULT;
  }

  public MachineModelLocation modelLocation() {
    return machine.getControllerModel();
  }

  public boolean hasData() {
    return data != null;
  }
}
