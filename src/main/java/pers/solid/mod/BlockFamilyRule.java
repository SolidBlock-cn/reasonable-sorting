package pers.solid.mod;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.solid.mod.mixin.BlockFamiliesAccessor;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 用于方块物品的排序规则。所有的楼梯、台阶都排在其基础物品之后。
 */
public class BlockFamilyRule implements Supplier<Map<Item, Collection<Item>>> {
    /**
     * 由方块到方块家族的映射。来组 {@link BlockFamily}。
     *
     * @see BlockFamily
     * @see net.minecraft.data.family.BlockFamilies
     */
    public static final Map<Block, BlockFamily> BASE_BLOCKS_TO_FAMILIES = BlockFamiliesAccessor.getBaseBlocksToFamilies();
    /**
     * 从栅栏映射到栅栏门的抽象映射。如果 {@link Configs#fenceGateFollowsFence} 为 <code>false</code>，则该映射视为空映射。
     */
    public static final Map<Item, Collection<Item>> FENCE_TO_FENCE_GATES = new AbstractMap<>() {
        @NotNull
        @Override
        public Set<Entry<Item, Collection<Item>>> entrySet() {
            return BlockFamilies.getFamilies().filter(b -> Configs.CONFIG_HOLDER.getConfig().fenceGateFollowsFence).map(blockFamily -> {
                final @Nullable Block v1 = blockFamily.getVariant(BlockFamily.Variant.FENCE);
                final @Nullable Block v2 = blockFamily.getVariant(BlockFamily.Variant.FENCE_GATE);
                if (v1 == null || v2 == null) return null;
                else
                    return new SimpleImmutableEntry<Item, Collection<Item>>(v1.asItem(), ObjectLists.singleton(v2.asItem()));
            }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
        }

        @Override
        public Collection<Item> get(Object key) {
            if (!Configs.CONFIG_HOLDER.getConfig().fenceGateFollowsFence || !(key instanceof BlockItem)) return null;
            final Block block = ((BlockItem) key).getBlock();
            for (BlockFamily blockFamily : BASE_BLOCKS_TO_FAMILIES.values()) {
                if (blockFamily.getVariant(BlockFamily.Variant.FENCE) == block) {
                    final @Nullable Block variant = blockFamily.getVariant(BlockFamily.Variant.FENCE_GATE);
                    return variant == null ? null : ObjectLists.singleton(variant.asItem());
                }
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }
    };
    /**
     * 对该数组内的变种应用上述排序，其他变种不受影响。
     * 应当与 {@link Configs#variantsFollowingBaseBlocks} 定义的默认值一致。
     */
    public static final List<BlockFamily.Variant> AFFECTED_VARIANTS = Lists.newArrayList(BlockFamily.Variant.STAIRS, BlockFamily.Variant.SLAB);
    /**
     * 抽象映射，由基础方块物品映射到其变种方块物品的集合。请注意该映射是抽象的，并不会实际存储内容，每调用一次 <code>get</code> 都会从 {@link #BASE_BLOCKS_TO_FAMILIES} 中获取，并临时构造一个不可修改的列表然后返回。
     */
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
                if (blockFamily == null) return null;
                for (BlockFamily.Variant variant : AFFECTED_VARIANTS) {
                    final @Nullable Block variant1 = blockFamily.getVariant(variant);
                    if (variant1 == null) continue;
                    builder.add(variant1.asItem());
                }
                return builder.build();
            } else if (key instanceof BlockItem) {
                return get(((BlockItem) key).getBlock());
            }
            return null;
        }
    };

    /**
     * 用于入口点。
     *
     * @return {@link #FENCE_TO_FENCE_GATES}
     */
    public static Map<Item, Collection<Item>> getFenceToFenceGates() {
        return FENCE_TO_FENCE_GATES;
    }

    @Override
    public Map<Item, Collection<Item>> get() {
        return BLOCK_ITEM_TO_VARIANTS;
    }
}
