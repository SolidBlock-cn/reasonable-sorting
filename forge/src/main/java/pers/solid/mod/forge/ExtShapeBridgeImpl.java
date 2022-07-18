package pers.solid.mod.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.GenericEvent;
import pers.solid.mod.ExtShapeBridge;
import pers.solid.mod.ReasonableSortingForge;

public class ExtShapeBridgeImpl {
  private static ExtShapeBridge value = null;

  public static ExtShapeBridge getInstance() {
    MinecraftForge.EVENT_BUS.post(new ExtShapeBridgeEvent());
    return value == null ? new ExtShapeBridge() : value;
  }

  public static final class ExtShapeBridgeEvent extends GenericEvent<ExtShapeBridge> {
    static public void setValue(ExtShapeBridge bridge) {
      if (value != null) {
        ReasonableSortingForge.LOGGER.warn("The ExtShapeBridgeEvent seems to have posted multime times! The value {} will override the existing value {}.", value, bridge);
      }
      value = bridge;
    }
  }
}
