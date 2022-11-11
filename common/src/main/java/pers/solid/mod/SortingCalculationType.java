package pers.solid.mod;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum SortingCalculationType implements StringIdentifiable {
  /**
   * 当游戏初始化、加载数据、排序规则改变时，计算一次排序后的迭代结果，此后就直接使用缓存好的迭代结果。
   */
  STANDARD,
  /**
   * 当游戏初始化、加载数据或者排序规则改变时，计算一次计算后的排序跟随映射，每次迭代时根据这个映射实时进行排序。
   */
  SEMI_REAL_TIME,
  /**
   * 每一次迭代时，实时计算排序结果。这会导致每次迭代注册表时（例如游戏初始化时、翻页时）出现卡顿。
   */
  REAL_TIME;

  private final String name;

  SortingCalculationType() {
    this.name = name().toLowerCase();
  }

  @Override
  public String asString() {
    return name;
  }

  public MutableText getName() {
    return Text.translatable("sortingCalculationType." + asString());
  }

  public MutableText getDescription() {
    return Text.translatable("sortingCalculationType." + asString() + ".description");
  }
}
