package pers.solid.mod;

import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 找到指定颜色（{@link #baseColor}）的方块和物品，其后面紧随 {@link #followingColors} 中的方块和物品。
 *
 * @param baseColor       基础颜色，如白色。
 * @param followingColors 方块或物品其后续跟随的颜色。
 * @param <T>             这个参数一般是方块或者物品。
 */
public record ColorSortingRule<T>(DyeColor baseColor, Iterable<DyeColor> followingColors) implements SortingRule<T> {
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public @Nullable Iterable<T> getFollowers(T leadingObj) {
    final Identifier identifier;
    if (leadingObj instanceof Block block) {
      identifier = Bridge.getBlockId(block);
    } else if (leadingObj instanceof Item item) {
      identifier = Bridge.getItemId(item);
    } else {
      return null;
    }
    if (identifier.getPath().contains(baseColor.asString())) {
      if (baseColor == DyeColor.BLUE || baseColor == DyeColor.GRAY) {
        if (identifier.getPath().contains("light_" + baseColor.asString())) return null;
      }
      return (Iterable<T>) (Iterable) Streams.stream(followingColors)
          .map(DyeColor::asString)
          .map(name -> new Identifier(identifier.getNamespace(), identifier.getPath().replace(baseColor.asString(), name)))
          .map(leadingObj instanceof Block ? Bridge::getBlockByIdOrEmpty : Bridge::getItemByIdOrEmpty)
          .filter(Optional::isPresent)
          .map(Optional::get)
          ::iterator;
    }
    return null;
  }
}
