package pers.solid.mod;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * 本类用于兼容不同平台。
 */
public class Bridge {
  @ExpectPlatform
  public static Identifier getBlockId(Block block) {
    return Registry.BLOCK.getId(block);
  }

  @ExpectPlatform
  public static Block getBlockById(Identifier identifier) {
    return Registry.BLOCK.get(identifier);
  }

  @ExpectPlatform
  public static Optional<Block> getBlockByIdOrEmpty(Identifier identifier) {
    return Registry.BLOCK.getOrEmpty(identifier);
  }

  public static Optional<Block> getBlockByIdOrWarn(Identifier identifier, Logger logger) {
    final Optional<Block> value = getBlockByIdOrEmpty(identifier);
    if (value.isEmpty()) {
      logger.warn("Unidentified block id: {}. This may be because the configuration is loaded before block is registered.", identifier);
    }
    return value;
  }

  @ExpectPlatform
  public static Identifier getItemId(Item item) {
    return Registry.ITEM.getId(item);
  }

  @ExpectPlatform
  public static Item getItemById(Identifier identifier) {
    return Registry.ITEM.get(identifier);
  }

  public static Optional<Item> getItemByIdOrEmpty(Identifier identifier) {
    return Registry.ITEM.getOrEmpty(identifier);
  }

  public static Optional<Item> getItemByIdOrWarn(Identifier identifier, Logger logger) {
    final Optional<Item> value = getItemByIdOrEmpty(identifier);
    if (value.isEmpty()) {
      logger.warn("Unidentified item id: {}. This may be because the configuration is loaded before item is registered.", identifier);
    }
    return value;
  }

  @ExpectPlatform
  public static Tag<Item> itemTag(Identifier id) {
    throw new AssertionError();
  }
}
