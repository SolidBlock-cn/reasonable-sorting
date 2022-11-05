package pers.solid.mod;

import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pers.solid.mod.interfaces.ItemGroupInterface;
import pers.solid.mod.mixin.BlockFamiliesAccessor;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 方块物品规则用于将方块排序规则应用到对应的物品上。当物品为方块物品上，就会应用对应的方块排序规则并转化为相应的物品。对非方块物品不起作用。
 *
 * @param rule 对应的方块规则。
 */
public record BlockItemRule(SortingRule<Block> rule) implements SortingRule<Item> {
  /**
   * 若物品为方块物品，则返回对应方块排序规则返回的方块对应物品的可迭代对象（集合）。若方块没有对应物品，则会略过。对于非方块物品，不产生作用。
   *
   * @param leadingObj 可能是方块物品的物品。
   * @return 对应的方块排序规则返回的结果（方块集合）对应的物品集合。
   *
   * Final verdict: needs from block relation, which has been in 1.19.2.
   * Currently, supports only from block relation-ship. Use custom transfer and sorting rules.
   */
  @Override
  public @Nullable Iterable<Item> getFollowers(Item leadingObj) {
    if (!(leadingObj instanceof BlockItem)) return null;

    //
    final Iterable<Block> followers = rule.getFollowers(((BlockItem)leadingObj).getBlock());
    return (followers == null ? null : Iterables.filter(Iterables.transform(followers, (IB)->{ var I = IB.asItem(); return I!=leadingObj ? I : null; }), Objects::nonNull));
  }

}
