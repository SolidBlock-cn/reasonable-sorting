package pers.solid.mod;

import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import pers.solid.mod.mixin.BaseBlocksToFamiliesAccessor;

import java.util.Map;

public class MixinHelper {
    public static final Map<Block, BlockFamily> BASE_BLOCKS_TO_FAMILIES = BaseBlocksToFamiliesAccessor.getBaseBlocksToFamilies();
    public static final BlockFamily.Variant[] BUILDING_BLOCK_VARIANTS = {BlockFamily.Variant.STAIRS,
            BlockFamily.Variant.SLAB};
//    public static final Set<Item> APPENDED_ITEMS = new HashSet<>();

    public static boolean isBuildingBlockContained(Block block) {
        // 检测某个方块是否已经是方块变种中的楼梯、台阶。
        for (var entry : BASE_BLOCKS_TO_FAMILIES.entrySet()) {
            var baseBlock = entry.getKey();
            if (baseBlock==block) return false;
            var blockFamily = entry.getValue();
            var variants = blockFamily.getVariants();
            for (var variant : BUILDING_BLOCK_VARIANTS) {
                if (variants.get(variant)==block) {
                    return true;
                }
            }
        }
        return false;
    }
}
