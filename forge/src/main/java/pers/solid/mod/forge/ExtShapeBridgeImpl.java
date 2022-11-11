package pers.solid.mod.forge;

import org.jetbrains.annotations.ApiStatus;
import pers.solid.mod.ExtShapeBridge;

import java.util.function.Supplier;

public class ExtShapeBridgeImpl {
  private static Supplier<ExtShapeBridge> valueSupplier = null;

  /**
   * 这是 {@link ExtShapeBridge#INSTANCE} 的值。当 {@link ExtShapeBridge} 初始化时，就会立即调用此值，并赋值给 final 变量。这个方法不应该由外部调用。
   */
  @ApiStatus.Internal
  public static ExtShapeBridge getInstance() {
    return valueSupplier == null ? new ExtShapeBridge() : valueSupplier.get();
  }

  /**
   * 在装有 Reasonable Sorting 的情况下，调用此函数，以提供 ExtShapeBridge。这个方法可以在较早的时候调用。
   */
  public static void setValue(Supplier<ExtShapeBridge> bridge) {
    if (valueSupplier != null) {
      ReasonableSortingForge.LOGGER.warn("The ExtShapeBridgeEvent seems to have posted multiple times! The value {} will override the existing value {}.", valueSupplier, bridge);
    } else {
      ReasonableSortingForge.LOGGER.info("Receiving ExtShapeBridge object.");
    }
    valueSupplier = bridge;
  }
}
