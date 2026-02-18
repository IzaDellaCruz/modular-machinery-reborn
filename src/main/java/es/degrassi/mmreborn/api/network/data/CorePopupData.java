package es.degrassi.mmreborn.api.network.data;

import es.degrassi.mmreborn.api.controller.CorePopup;
import es.degrassi.mmreborn.api.network.Data;
import es.degrassi.mmreborn.common.registration.DataRegistration;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CorePopupData extends Data<CorePopup> {

  public CorePopupData(short id, CorePopup value) {
    super(DataRegistration.CORE_POPUP_DATA.get(), id, value);
  }

  public CorePopupData(short id, RegistryFriendlyByteBuf buffer) {
    this(id, CorePopup.STREAM_CODEC.decode(buffer));
  }

  @Override
  public void writeData(RegistryFriendlyByteBuf buffer) {
    super.writeData(buffer);
    CorePopup.STREAM_CODEC.encode(buffer, getValue());
  }
}
