package es.degrassi.mmreborn.api.network.data;

import es.degrassi.mmreborn.api.network.Data;
import es.degrassi.mmreborn.common.registration.DataRegistration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationData extends Data<ResourceLocation> {
  public ResourceLocationData(short id, ResourceLocation value) {
    super(DataRegistration.RESOURCE_LOCATION_DATA.get(), id, value);
  }

  public ResourceLocationData(short id, RegistryFriendlyByteBuf buffer) {
    this(id, buffer.readResourceLocation());
  }

  @Override
  public void writeData(RegistryFriendlyByteBuf buffer) {
    super.writeData(buffer);
    buffer.writeResourceLocation(getValue());
  }
}
