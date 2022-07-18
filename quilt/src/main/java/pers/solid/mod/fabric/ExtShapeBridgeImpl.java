package pers.solid.mod.fabric;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import pers.solid.mod.ExtShapeBridge;

public class ExtShapeBridgeImpl {
  public static @NotNull ExtShapeBridge getInstance() {
    for (ExtShapeBridge entrypoint : QuiltLoader.getEntrypoints("reasonable-sorting:extshape-bridge", ExtShapeBridge.class)) {
      return entrypoint;
    }
    return new ExtShapeBridge();
  }
}
