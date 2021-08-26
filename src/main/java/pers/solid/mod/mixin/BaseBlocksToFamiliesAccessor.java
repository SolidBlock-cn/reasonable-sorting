package pers.solid.mod.mixin;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import pers.solid.mod.BlockFamilies;
import pers.solid.mod.BlockFamily;

import java.util.Map;

@Mixin(BlockFamilies.class)
public interface BaseBlocksToFamiliesAccessor {
    @Accessor("BASE_BLOCKS_TO_FAMILIES")
    static Map<Block, BlockFamily> getBaseBlocksToFamilies() {
        throw new AssertionError();
    }
}
