package pers.solid.mod.fabric;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registries;
import net.minecraft.util.registry.Registry;

public class BridgeImpl {
  public static Identifier getItemId(Item item) {
    return Registries.ITEM.getId(item);
  }

  public static Item getItemById(Identifier identifier) {
    return Registries.ITEM.get(identifier);
  }

  public static boolean itemIdExists(Identifier identifier) {
    final boolean b = Registries.ITEM.containsId(identifier);
    if (!b) {
      ReasonableSortingFabric.LOGGER.warn("Unidentified item id: {}. This may be because the configuration is loaded before item is registered.", identifier);
    }
    return b;
  }
}
