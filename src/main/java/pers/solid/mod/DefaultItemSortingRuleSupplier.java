package pers.solid.mod;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultItemSortingRuleSupplier implements Supplier<Collection<Map<Item, Collection<Item>>>> {
    /**
     * 此模组默认内置的物品组合规则。
     */
    public static final ImmutableMap<Item, Collection<Item>> DEFAULT_ITEM_SORTING_RULE = new ImmutableMap.Builder<Item, Collection<Item>>()
            .put(Items.COBBLESTONE, ImmutableList.of(Items.MOSSY_COBBLESTONE))
            .put(Items.SANDSTONE, ImmutableList.of(Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE, Items.RED_SANDSTONE, Items.CHISELED_RED_SANDSTONE, Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE))
            .put(Items.ICE, ImmutableList.of(Items.PACKED_ICE, Items.BLUE_ICE))
            .put(Items.NETHER_BRICKS, ImmutableList.of(Items.CRACKED_NETHER_BRICKS, Items.RED_NETHER_BRICKS))
            .put(Items.QUARTZ_BLOCK, ImmutableList.of(Items.SMOOTH_QUARTZ, Items.CHISELED_QUARTZ_BLOCK, Items.QUARTZ_BRICKS, Items.QUARTZ_PILLAR))
            .put(Items.OAK_SLAB, ImmutableList.of(Items.PETRIFIED_OAK_SLAB))
            .put(Items.SMOOTH_STONE, ImmutableList.of(Items.SMOOTH_STONE_SLAB))
            .put(Items.BOOK, ImmutableList.of(Items.WRITABLE_BOOK))
            .put(Items.PAPER, ImmutableList.of(Items.MAP))
            .put(Items.GOLD_NUGGET, ImmutableList.of(Items.IRON_NUGGET))
            .put(Items.BRICK, ImmutableList.of(Items.NETHER_BRICK))
            .put(Items.WHEAT_SEEDS, ImmutableList.of(Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS))
            .put(Items.SNOWBALL, ImmutableList.of(Items.CLAY_BALL, Items.ENDER_PEARL, Items.ENDER_EYE))
            .put(Items.BOW, ImmutableList.of(Items.CROSSBOW))
            .put(Items.ARROW, ImmutableList.of(Items.TRIDENT, Items.SHIELD, Items.TOTEM_OF_UNDYING))
            .put(Items.GHAST_TEAR, ImmutableList.of(Items.FERMENTED_SPIDER_EYE, Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.BREWING_STAND, Items.CAULDRON, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT, Items.RABBIT_FOOT, Items.PHANTOM_MEMBRANE, Items.GLASS_BOTTLE, Items.DRAGON_BREATH))
            .put(Items.FLINT, ImmutableList.of(Items.SNOWBALL, Items.LEATHER)).build();

    /**
     * @return 会转射到 {@link #DEFAULT_ITEM_SORTING_RULE} 的 {@link ForwardingMap}。当对应的配置为 <code>true</code> 是转发到上述映射，否则转发到不可变的空映射。
     */
    @Override
    public Collection<Map<Item, Collection<Item>>> get() {
        return Collections.singleton(new ForwardingMap<Item, Collection<Item>>() {
            @Override
            protected Map<Item, Collection<Item>> delegate() {
                return Configs.CONFIG_HOLDER.getConfig().enableDefaultItemSortingRules ? DEFAULT_ITEM_SORTING_RULE : ImmutableMap.of();
            }
        });
    }
}
