package pers.solid.mod;

import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * BaseToVariantRule 是让特定的方块变种的方块紧随其基础方块的规则。例如，让所有的楼梯和台阶紧随其基础方块。
 */
public final class BaseToVariantRule implements SortingRule<Block> {
  private final Predicate<Block> blockPredicate;
  private final Iterable<BlockFamily.Variant> variants;

  /**
   * @param blockPredicate 方块应用此规则所需要的条件。
   * @param variants       需要跟随在其后的方块变种。
   */
  public BaseToVariantRule(Predicate<Block> blockPredicate, Iterable<BlockFamily.Variant> variants) {
    this.blockPredicate = blockPredicate;
    this.variants = variants;
  }

  public Predicate<Block> blockPredicate() {
    return blockPredicate;
  }

  public Iterable<BlockFamily.Variant> variants() {
    return variants;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    BaseToVariantRule that = (BaseToVariantRule) obj;
    return Objects.equals(this.blockPredicate, that.blockPredicate) &&
        Objects.equals(this.variants, that.variants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockPredicate, variants);
  }

  @Override
  public String toString() {
    return "BaseToVariantRule[" +
        "blockPredicate=" + blockPredicate + ", " +
        "variants=" + variants + ']';
  }

  /**
   * 每个基础方块的跟随者是其对应变种的方块。非基础方块会被略过。
   *
   * @param block 可能是基础方块的方块。
   * @return 由方块对应的变种组成的流。若方块不是基础方块，则返回 null。
   * @see pers.solid.mod.BlockFamily
   */
  @Override
  public @Nullable Iterable<Block> getFollowers(Block block) {
    if (!blockPredicate.test(block)) return null;
    final @Nullable BlockFamily blockFamily = BlockFamilies.BASE_BLOCKS_TO_FAMILIES.get(block);
    if (blockFamily == null) return null;
    return Streams.stream(variants).map(blockFamily::getVariant).filter(Objects::nonNull)::iterator;
  }
}
