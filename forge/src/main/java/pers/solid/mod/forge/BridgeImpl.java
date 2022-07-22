package pers.solid.mod.forge;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

public class BridgeImpl {
  public static Identifier getItemId(Item item) {
    return ForgeRegistries.ITEMS.getKey(item);
  }

  public static Item getItemById(Identifier identifier) {
    return ForgeRegistries.ITEMS.getValue(identifier);
  }

  public static boolean itemIdExists(Identifier identifier) {
    final boolean b = ForgeRegistries.ITEMS.containsKey(identifier);
    if (!b) {
      ReasonableSortingForge.LOGGER.warn("Unidentified item id: {}. This may be because the configuration is loaded before item is registered.", identifier);
    }
    return b;
  }
}
