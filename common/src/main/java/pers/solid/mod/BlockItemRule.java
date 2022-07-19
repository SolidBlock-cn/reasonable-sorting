package pers.solid.mod;

import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 方块物品规则用于将方块排序规则应用到对应的物品上。当物品为方块物品上，就会应用对应的方块排序规则并转化为相应的物品。对非方块物品不起作用。
 */
public final class BlockItemRule implements SortingRule<Item> {
  private final SortingRule<Block> rule;

  /**
   * @param rule 对应的方块规则。
   */
  public BlockItemRule(SortingRule<Block> rule) {
    this.rule = rule;
  }

  public SortingRule<Block> rule() {
    return rule;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    BlockItemRule that = (BlockItemRule) obj;
    return Objects.equals(this.rule, that.rule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rule);
  }

  @Override
  public String toString() {
    return "BlockItemRule[" +
        "rule=" + rule + ']';
  }

  /**
   * 若物品为方块物品，则返回对应方块排序规则返回的方块对应物品的可迭代对象（集合）。若方块没有对应物品，则会略过。对于非方块物品，不产生作用。
   *
   * @param leadingObj 可能是方块物品的物品。
   * @return 对应的方块排序规则返回的结果（方块集合）对应的物品集合。
   */
  @Override
  public @Nullable Iterable<Item> getFollowers(Item leadingObj) {
    if (!(leadingObj instanceof BlockItem)) return null;
    final Iterable<Block> followers = rule.getFollowers(((BlockItem) leadingObj).getBlock());
    return followers == null ? null : Iterables.filter(Iterables.transform(followers, Block::asItem), Objects::nonNull);
  }
}
