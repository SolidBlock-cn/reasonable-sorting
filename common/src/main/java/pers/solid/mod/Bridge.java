package pers.solid.mod;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registries;
import net.minecraft.util.registry.Registry;

/**
 * 本类用于兼容不同平台。
 */
public class Bridge {
  @ExpectPlatform
  public static Identifier getItemId(Item item) {
    return Registries.ITEM.getId(item);
  }

  @ExpectPlatform
  public static Item getItemById(Identifier identifier) {
    return Registries.ITEM.get(identifier);
  }

  @ExpectPlatform
  public static boolean itemIdExists(Identifier identifier) {
    return Registries.ITEM.containsId(identifier);
  }
}
