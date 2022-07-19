package pers.solid.mod;

import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 让一个方块变种被特定相应方块变种的方块跟随的排序规则。例如，让所有的栅栏都被对应的栅栏门跟随:
 * <pre>
 *   {@code new VariantToVariantRule(
 *       predicate = block -> block instanceof FenceBlock,
 *     variantFrom = BlockFamily.Variant.FENCE,
 *       variantTo = Collections.singleton(BlockFamily.Variant.FENCE_GATE))}
 * </pre>
 */
public final class VariantToVariantRule implements SortingRule<Block> {
  private final Predicate<Block> blockPredicate;
  private final BlockFamily.Variant variantFrom;
  private final Iterable<BlockFamily.Variant> variantTo;

  /**
   * @param blockPredicate 应用此规则的方块需要满足的条件。
   * @param variantFrom    被跟随的方块变种。
   * @param variantTo      跟随的方块变种，这些变种的对应方块会跟随在后面。可以是多个值。
   */
  public VariantToVariantRule(Predicate<Block> blockPredicate, BlockFamily.Variant variantFrom, Iterable<BlockFamily.Variant> variantTo) {
    this.blockPredicate = blockPredicate;
    this.variantFrom = variantFrom;
    this.variantTo = variantTo;
  }

  public Predicate<Block> blockPredicate() {
    return blockPredicate;
  }

  public BlockFamily.Variant variantFrom() {
    return variantFrom;
  }

  public Iterable<BlockFamily.Variant> variantTo() {
    return variantTo;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    VariantToVariantRule that = (VariantToVariantRule) obj;
    return Objects.equals(this.blockPredicate, that.blockPredicate) &&
        Objects.equals(this.variantFrom, that.variantFrom) &&
        Objects.equals(this.variantTo, that.variantTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockPredicate, variantFrom, variantTo);
  }

  @Override
  public String toString() {
    return "VariantToVariantRule[" +
        "blockPredicate=" + blockPredicate + ", " +
        "variantFrom=" + variantFrom + ", " +
        "variantTo=" + variantTo + ']';
  }

  /**
   * 若方块本身是 {@link #variantFrom} 中的方块变种，则会被对应的 {@code variantTo} 中的方块变种跟随。
   *
   * @param block 可能属于 {@link #variantFrom} 中的变种的方块。
   * @return 对应的 {@code variantTo} 中的方块变种的方块。若方块不属于该变种，或不符合 {@link #blockPredicate}，则返回 {@code null}。
   */
  @Override
  public @Nullable Iterable<Block> getFollowers(Block block) {
    for (BlockFamily blockFamily : BlockFamilies.BASE_BLOCKS_TO_FAMILIES.values()) {
      if (blockPredicate.test(block) && blockFamily.getVariant(variantFrom) == block) {
        return Streams.stream(variantTo).map(blockFamily::getVariant).filter(Objects::nonNull)::iterator;
      }
    }
    return null;
  }
}
