package pers.solid.mod;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import pers.solid.mod.mixin.BaseBlocksToFamiliesAccessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MixinHelper {
    public static final Map<Block, BlockFamily> BASE_BLOCKS_TO_FAMILIES = BaseBlocksToFamiliesAccessor.getBaseBlocksToFamilies();
    public static final BlockFamily.Variant[] BUILDING_BLOCK_VARIANTS = {BlockFamily.Variant.STAIRS,
            BlockFamily.Variant.SLAB};

    public static final Map<Item, Item[]> ITEM_COMBINATIONS = new HashMap<>();

    static {
        ITEM_COMBINATIONS.put(Items.COBBLESTONE, new Item[]{Items.MOSSY_COBBLESTONE});
        ITEM_COMBINATIONS.put(Items.SANDSTONE, new Item[]{Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE,
                Items.SMOOTH_SANDSTONE, Items.RED_SANDSTONE, Items.CHISELED_RED_SANDSTONE, Items.CUT_RED_SANDSTONE,
                Items.SMOOTH_RED_SANDSTONE});
        ITEM_COMBINATIONS.put(Items.ICE, new Item[]{Items.PACKED_ICE, Items.BLUE_ICE});
        ITEM_COMBINATIONS.put(Items.NETHER_BRICKS, new Item[]{Items.CRACKED_NETHER_BRICKS, Items.RED_NETHER_BRICKS});
        ITEM_COMBINATIONS.put(Items.QUARTZ_BLOCK, new Item[]{Items.SMOOTH_QUARTZ, Items.CHISELED_QUARTZ_BLOCK,
                Items.QUARTZ_BRICKS,
                Items.QUARTZ_PILLAR});
        ITEM_COMBINATIONS.put(Items.OAK_SLAB, new Item[]{Items.PETRIFIED_OAK_SLAB});
        ITEM_COMBINATIONS.put(Items.SMOOTH_STONE, new Item[]{Items.SMOOTH_STONE_SLAB});
        ITEM_COMBINATIONS.put(Items.BOOK, new Item[]{Items.WRITABLE_BOOK});
        ITEM_COMBINATIONS.put(Items.PAPER, new Item[]{Items.MAP});
        ITEM_COMBINATIONS.put(Items.GOLD_NUGGET, new Item[]{Items.IRON_NUGGET});
        ITEM_COMBINATIONS.put(Items.BRICK, new Item[]{Items.NETHER_BRICK});
        ITEM_COMBINATIONS.put(Items.WHEAT_SEEDS, new Item[]{Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS});
        ITEM_COMBINATIONS.put(Items.SNOWBALL, new Item[]{Items.CLAY_BALL, Items.ENDER_PEARL, Items.ENDER_EYE});
        ITEM_COMBINATIONS.put(Items.BOW, new Item[]{Items.CROSSBOW});
        ITEM_COMBINATIONS.put(Items.ARROW, new Item[]{Items.TRIDENT, Items.SHIELD, Items.TOTEM_OF_UNDYING});
        ITEM_COMBINATIONS.put(Items.GHAST_TEAR, new Item[]{Items.FERMENTED_SPIDER_EYE, Items.BLAZE_POWDER,
                Items.MAGMA_CREAM, Items.BREWING_STAND, Items.CAULDRON, Items.GLISTERING_MELON_SLICE,
                Items.GOLDEN_CARROT, Items.RABBIT_FOOT, Items.PHANTOM_MEMBRANE, Items.GLASS_BOTTLE, Items.DRAGON_BREATH});
        ITEM_COMBINATIONS.put(Items.FLINT, new Item[]{Items.SNOWBALL, Items.LEATHER});

        ITEM_COMBINATIONS.put(Items.WARPED_TRAPDOOR,new Item[]{Items.IRON_TRAPDOOR});
        ITEM_COMBINATIONS.put(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE,new Item[]{Items.LIGHT_WEIGHTED_PRESSURE_PLATE
                ,Items.HEAVY_WEIGHTED_PRESSURE_PLATE});
        ITEM_COMBINATIONS.put(Items.WARPED_FENCE,new Item[]{Items.NETHER_BRICK_FENCE});
    }

    public static boolean isBuildingBlockContained(Block block) {
        // 检测某个方块是否已经是方块变种中的楼梯、台阶。
        for (var entry : BASE_BLOCKS_TO_FAMILIES.entrySet()) {
            var baseBlock = entry.getKey();
            if (baseBlock == block) return false;
            var blockFamily = entry.getValue();
            var variants = blockFamily.getVariants();
            for (var variant : BUILDING_BLOCK_VARIANTS) {
                if (variants.get(variant) == block) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBuildingBlockContained(Item item) {
        if (item instanceof BlockItem) {
            return isBuildingBlockContained(((BlockItem) item).getBlock());
        } else {
            return false;
        }
    }

    public static boolean isCombinationLeader(Item item) {
        // 检测某个物品是否为其组合的开始物品。
        return ITEM_COMBINATIONS.containsKey(item);
    }

    public static boolean isCombinationFollower(Item item) {
        // 检测某个物品是否为某个物品组合中的跟随物品。
        for (var val : ITEM_COMBINATIONS.values()) {
            if (List.of(val).contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static void applyBlockItemWithVariants(Block block, Consumer<Item> action) {
        if (BASE_BLOCKS_TO_FAMILIES.containsKey(block)) {
            BlockFamily blockFamily = BASE_BLOCKS_TO_FAMILIES.get(block);
            Map<BlockFamily.Variant, Block> variants = blockFamily.getVariants();
            for (var variant : BUILDING_BLOCK_VARIANTS) {
                if (variants.containsKey(variant)) {
                    Block variantBlock = variants.get(variant);
                    action.accept(variantBlock.asItem());
                }
            }
        }
    }

    public static void applyItemWithVariants(Item item, Consumer<Item> action) {
        action.accept(item);
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            applyBlockItemWithVariants(block, i -> applyItemWithFollowings(i, action));
        }
    }

    public static void applyItemWithFollowings(Item item, Consumer<Item> action) {
        applyItemWithVariants(item, action);
        if (isCombinationLeader(item)) {
            var followingItems = ITEM_COMBINATIONS.get(item);
            for (var item1 : followingItems) {
                applyItemWithFollowings(item1, action);
            }
        }
    }

    public static Iterator<Item> itemRegistryIterator(ObjectList<Item> rawIdToEntry) {
        ObjectList<Item> list = new ObjectArrayList<>();
        for (Item item : rawIdToEntry) {
            if (isCombinationFollower(item)) continue;
            if (isBuildingBlockContained(item)) continue;
            applyItemWithFollowings(item, list::add);
        }
        return list.iterator();
    }
}
