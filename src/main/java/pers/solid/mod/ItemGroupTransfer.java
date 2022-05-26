package pers.solid.mod;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 转移物品组。
 */
public class ItemGroupTransfer implements Supplier<@Unmodifiable Collection<@Unmodifiable Map<Item, ItemGroup>>> {
  public static final @Unmodifiable Map<Item, ItemGroup> DEFAULT_TRANSFER_RULES =
      new AbstractMap<>() {
        @NotNull
        @Override
        public @Unmodifiable ImmutableSet<Map.Entry<Item, ItemGroup>> entrySet() {
          ImmutableSet.Builder<Map.Entry<Item, ItemGroup>> entrySet = new ImmutableSet.Builder<>();
          for (Item item : Registry.ITEM) {
            final @Nullable ItemGroup itemGroup = this.get(item);
            if (itemGroup != null) {
              try {
                entrySet.add(new SimpleEntry<>(item, itemGroup));
              } catch (ClassCastException ignore) {
              }
            }
          }
          return entrySet.build();
        }

        @Override
        public ItemGroup get(Object key) {
          final Configs config = Configs.CONFIG_HOLDER.getConfig();
          if (key instanceof BlockItem blockItem && blockItem.getGroup() == ItemGroup.REDSTONE) {
            final Block block = blockItem.getBlock();
            if (config.buttonsInDecorations
                && block instanceof AbstractButtonBlock) {
              return ItemGroup.DECORATIONS;
            } else if (config.fenceGatesInDecorations
                && block instanceof FenceGateBlock) {
              return ItemGroup.DECORATIONS;
            } else if (config.doorsInDecorations
                && block instanceof DoorBlock) {
              return ItemGroup.DECORATIONS;
            }
          } else if (config.swordsInTools
              && key instanceof SwordItem swordItem
              && swordItem.getGroup() == ItemGroup.COMBAT) {
            return ItemGroup.TOOLS;
          }
          return null;
        }

        @Override
        public boolean containsKey(Object key) {
          return get(key) != null;
        }
      };

  @Override
  public @Unmodifiable Collection<@Unmodifiable Map<Item, ItemGroup>> get() {
    return Collections.singleton(DEFAULT_TRANSFER_RULES);
  }
}
