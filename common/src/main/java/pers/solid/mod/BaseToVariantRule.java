package pers.solid.mod;

import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import org.jetbrains.annotations.Nullable;
import pers.solid.mod.mixin.BlockFamiliesAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * BaseToVariantRule 是让特定的方块变种的方块紧随其基础方块的规则。例如，让所有的楼梯和台阶紧随其基础方块。
 *
 * @param blockPredicate 方块应用此规则所需要的条件。
 * @param variants       需要跟随在其后的方块变种。
 */
public record BaseToVariantRule(Predicate<Block> blockPredicate, Iterable<BlockFamily.Variant> variants) implements SortingRule<Block> {
  /**
   * 每个基础方块的跟随者是其对应变种的方块。非基础方块会被略过。
   *
   * @param block 可能是基础方块的方块。
   * @return 由方块对应的变种组成的流。若方块不是基础方块，则返回 null。
   * @see BlockFamily
   *
   * Final verdict: needs from block relation, which has been in 1.19.2.
   * Currently, supports only from block relation-ship. Use custom transfer and sorting rules.
   */
  @Override
  public @Nullable Iterable<Block> getFollowers(Block block) {
    if (!blockPredicate.test(block)) return null;
    var variants_ = new ArrayList<BlockFamily.Variant>(Streams.stream(variants).toList());
    var registry = BlockFamiliesAccessor.getBaseBlocksToFamilies();
    var blockFamily = registry.get(block); if (blockFamily == null) return null;

    // also, don't repeat!
    var finalBlockFamily = blockFamily;
    return variants_.stream().map((V)->{ return finalBlockFamily.getVariant(V); }).filter(Objects::nonNull)::iterator;
  }
}
