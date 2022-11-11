package pers.solid.mod;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

/**
 * 指定本模组修改的排序可以影响哪些范围
 */
public enum SortingInfluenceRange implements StringIdentifiable {
  REGISTRY,
  INVENTORY_ONLY;
  private final String name;

  SortingInfluenceRange() {
    this.name = name().toLowerCase();
  }

  @Override
  public String asString() {
    return name;
  }

  public MutableText getName() {
    return Text.translatable("sortingInfluenceRange." + asString());
  }

  public MutableText getDescription() {
    return Text.translatable("sortingInfluenceRange." + asString() + ".description");
  }
}
