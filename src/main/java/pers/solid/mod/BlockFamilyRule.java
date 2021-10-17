package pers.solid.mod;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.solid.mod.mixin.BaseBlocksToFamiliesAccessor;

import java.util.*;
import java.util.function.Supplier;

public class BlockFamilyRule implements Supplier<Map<Item, Collection<Item>>> {
    public static final Map<Block, BlockFamily> BASE_BLOCKS_TO_FAMILIES = BaseBlocksToFamiliesAccessor.getBaseBlocksToFamilies();
    public static final Map<Item, Collection<Item>> BLOCK_ITEM_TO_VARIANTS = new AbstractMap<>() {
        @NotNull
        @Override
        public Set<Entry<Item, Collection<Item>>> entrySet() {
            Set<Entry<Item, Collection<Item>>> entrySet = new HashSet<>();
            for (Entry<Block, BlockFamily> entry : BASE_BLOCKS_TO_FAMILIES.entrySet()) {
                entrySet.add(new SimpleEntry<>(entry.getKey().asItem(), get(((Block) entry.getKey()))));
            }
            return entrySet;
        }

        @NotNull
        @Override
        public Collection<Collection<Item>> values() {
            return super.values();
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof BlockItem) {
                return BASE_BLOCKS_TO_FAMILIES.containsKey(((BlockItem) key).getBlock());
            } else if (key instanceof Block) {
                return BASE_BLOCKS_TO_FAMILIES.containsKey(key);
            }
            return false;
        }

        @Override
        public Collection<Item> get(Object key) {
            if (key instanceof Block) {
                ImmutableList.Builder<Item> builder = new ImmutableList.Builder<>();
                final @Nullable BlockFamily blockFamily = BASE_BLOCKS_TO_FAMILIES.get(key);
                if (blockFamily==null) return null;
                for (BlockFamily.Variant variant : BUILDING_BLOCK_VARIANTS) {
                    final @Nullable Block variant1 = blockFamily.getVariant(variant);
                    if (variant1==null) continue;
                    builder.add(variant1.asItem());
                }
                return builder.build();
            } else if (key instanceof BlockItem) {
                return get(((BlockItem) key).getBlock());
            }
            return null;
        }
    };
    public static final BlockFamily.Variant[] BUILDING_BLOCK_VARIANTS = {BlockFamily.Variant.STAIRS, BlockFamily.Variant.SLAB};

    @Override
    public Map<Item, Collection<Item>> get() {
        return BLOCK_ITEM_TO_VARIANTS;
    }
}
