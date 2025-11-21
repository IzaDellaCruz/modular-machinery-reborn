package es.degrassi.mmreborn.common.crafting.modifier;

import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public interface IRecipeModifier<
    R extends IRequirement<C, T>,
    C extends MachineComponent<T>,
    T
    > {

  boolean shouldApply(RequirementType<R, C, T> type, IOType mode);

  float apply(float original);

  Component getTooltip();

  Component getDefaultTooltip();

  enum OPERATION {
    ADDITION,
    MULTIPLICATION;

    public static final NamedCodec<OPERATION> CODEC = NamedCodec.enumCodec(OPERATION.class);

    public static OPERATION value(String value) {
      return valueOf(value.toUpperCase(Locale.ROOT));
    }
  }
}
