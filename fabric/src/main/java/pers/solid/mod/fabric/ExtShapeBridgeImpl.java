package pers.solid.mod.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import pers.solid.mod.ExtShapeBridge;

public class ExtShapeBridgeImpl {
  public static @NotNull ExtShapeBridge getInstance() {
    for (ExtShapeBridge entrypoint : FabricLoader.getInstance().getEntrypoints("reasonable-sorting:extshape-bridge", ExtShapeBridge.class)) {
      return entrypoint;
    }
    return new ExtShapeBridge();
  }
}
