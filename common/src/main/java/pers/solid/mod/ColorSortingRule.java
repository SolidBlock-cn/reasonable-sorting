package pers.solid.mod;

import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * 找到指定颜色（{@link #baseColor}）的方块和物品，其后面紧随 {@link #followingColors} 中的方块和物品。
 *
 * @param <T> 这个参数一般是方块或者物品。
 */
public final class ColorSortingRule<T> implements SortingRule<T> {
  private final DyeColor baseColor;
  private final Iterable<DyeColor> followingColors;

  /**
   * @param baseColor       基础颜色，如白色。
   * @param followingColors 方块或物品其后续跟随的颜色。
   */
  ColorSortingRule(DyeColor baseColor, Iterable<DyeColor> followingColors) {
    this.baseColor = baseColor;
    this.followingColors = followingColors;
  }

  public DyeColor baseColor() {
    return baseColor;
  }

  public Iterable<DyeColor> followingColors() {
    return followingColors;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    ColorSortingRule<?> that = (ColorSortingRule<?>) obj;
    return Objects.equals(this.baseColor, that.baseColor) &&
        Objects.equals(this.followingColors, that.followingColors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseColor, followingColors);
  }

  @Override
  public String toString() {
    return "ColorSortingRule[" +
        "baseColor=" + baseColor + ", " +
        "followingColors=" + followingColors + ']';
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public @Nullable Iterable<T> getFollowers(T leadingObj) {
    final Identifier identifier;
    if (leadingObj instanceof Block) {
      identifier = Bridge.getBlockId((Block) leadingObj);
    } else if (leadingObj instanceof Item) {
      identifier = Bridge.getItemId((Item) leadingObj);
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
