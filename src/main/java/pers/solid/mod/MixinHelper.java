package pers.solid.mod;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.*;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
    public static boolean allowAppendingAnyBlock = false;

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
        ITEM_COMBINATIONS.put(Items.OAK_SLAB,new Item[]{Items.PETRIFIED_OAK_SLAB});
        ITEM_COMBINATIONS.put(Items.SMOOTH_STONE,new Item[]{Items.SMOOTH_STONE_SLAB});
        ITEM_COMBINATIONS.put(Items.BOOK,new Item[]{Items.WRITABLE_BOOK});
        ITEM_COMBINATIONS.put(Items.PAPER,new Item[]{Items.MAP});
        ITEM_COMBINATIONS.put(Items.GOLD_NUGGET,new Item[]{Items.IRON_NUGGET});
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

    public static boolean isCombinationLeader(Item item) {
        // 检测某个物品是否为其组合的开始物品。
        return ITEM_COMBINATIONS.containsKey(item);
    }

    public static boolean isCombinationFollower(Item item) {
        // 检测某个物品是否为某个物品组合中的跟随物品。
        for (var val : ITEM_COMBINATIONS.values()) {
            if (List.of(val).contains(item)) return true;
        }
        return false;
    }

    public static void appendCombinedItems(Item item, ItemGroup group, DefaultedList<ItemStack> stacks,
                                           CallbackInfo ci) {
        if (isCombinationLeader(item)) {
			var followingItems = ITEM_COMBINATIONS.get(item);
			for (var item1 : followingItems) {
			    allowAppendingAnyBlock = true;
				item1.appendStacks(group,stacks);
				allowAppendingAnyBlock = false;
			}
		}
    }

    public static void appendMoreBuildingBlocks(Item item, ItemGroup group, DefaultedList<ItemStack> stacks,
                                                Block block) {
        if (group!=ItemGroup.BUILDING_BLOCKS) return;
        if (BASE_BLOCKS_TO_FAMILIES.containsKey(block)) {
            BlockFamily blockFamily = BASE_BLOCKS_TO_FAMILIES.get(block);
            Map<BlockFamily.Variant, Block> variants = blockFamily.getVariants();
            for (var variant : BUILDING_BLOCK_VARIANTS) {
                if (variants.containsKey(variant)) {
                    Block block1 = variants.get(variant);
                    allowAppendingAnyBlock = true;
                    block1.asItem().appendStacks(group, stacks);
                    allowAppendingAnyBlock = false;
                }
            }
        }
    }

    public static void applyBlockWithVariants(Block block, Consumer<Block> action) {
        if (isBuildingBlockContained(block)) return;
        action.accept(block);
        if (BASE_BLOCKS_TO_FAMILIES.containsKey(block)) {
            BlockFamily blockFamily = BASE_BLOCKS_TO_FAMILIES.get(block);
            Map<BlockFamily.Variant, Block> variants = blockFamily.getVariants();
            for (var variant : BUILDING_BLOCK_VARIANTS) {
                if (variants.containsKey(variant)) {
                    Block block1 = variants.get(variant);
                    action.accept(block1);
                }
            }
        }
    }

    public static void applyItemWithVariants(Item item, Consumer<Item> action) {
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            applyBlockWithVariants(block,block1 -> action.accept(block1.asItem()));
        } else {
            action.accept(item);
        }
    }

    public static Iterator<Item> itemRegistryIterator(ObjectList<Item> rawIdToEntry) {
        ObjectList<Item> list = new ObjectArrayList<>();
        for (Item item : rawIdToEntry) {
            if (isCombinationFollower(item)) continue;
            applyItemWithVariants(item,list::add);
            if (isCombinationLeader(item)) {
                var followingItems = ITEM_COMBINATIONS.get(item);
                for (var item1 : followingItems) {
                    applyItemWithVariants(item1,list::add);
                }
            }
        }
        return list.iterator();
    }
}
