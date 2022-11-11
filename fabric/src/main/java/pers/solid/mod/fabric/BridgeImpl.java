package pers.solid.mod.fabric;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class BridgeImpl {
  public static Identifier getItemId(Item item) {
    return Registry.ITEM.getId(item);
  }

  public static Item getItemById(Identifier identifier) {
    return Registry.ITEM.get(identifier);
  }

  public static Block getBlockById(Identifier identifier) {
    return Registry.BLOCK.get(identifier);
  }

  public static Identifier getBlockId(Block block) {
    return Registry.BLOCK.getId(block);
  }

  public static Optional<Block> getBlockByIdOrEmpty(Identifier identifier) {
    return Registry.BLOCK.getOrEmpty(identifier);
  }

  public static Tag<Item> itemTag(Identifier id) {
    return TagFactory.ITEM.create(id);
  }
}
