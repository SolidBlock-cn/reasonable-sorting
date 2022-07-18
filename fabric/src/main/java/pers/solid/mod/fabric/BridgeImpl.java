package pers.solid.mod.fabric;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BridgeImpl {
  public static Identifier getItemId(Item item) {
    return Registry.ITEM.getId(item);
  }

  public static Item getItemById(Identifier identifier) {
    return Registry.ITEM.get(identifier);
  }

  public static boolean itemIdExists(Identifier identifier) {
    return Registry.ITEM.containsId(identifier);
  }
}
