package pers.solid.mod.forge;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class BridgeImpl {

  public static Identifier getItemId(Item item) {
    return ForgeRegistries.ITEMS.getKey(item);
  }

  public static Item getItemById(Identifier identifier) {
    return ForgeRegistries.ITEMS.getValue(identifier);
  }

  public static Block getBlockById(Identifier identifier) {
    return ForgeRegistries.BLOCKS.getValue(identifier);
  }

  public static Identifier getBlockId(Block block) {
    return ForgeRegistries.BLOCKS.getKey(block);
  }

  public static Optional<Block> getBlockByIdOrEmpty(Identifier identifier) {
    return ForgeRegistries.BLOCKS.getDelegate(identifier).map(RegistryEntry.Reference::value);
  }
}
