package pers.solid.mod;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import oshi.annotation.concurrent.Immutable;

import java.util.Iterator;

/** 一种用于模组内部的特殊的物品组。不会实际上显示。 */
@Immutable
@Deprecated
public class ItemGroupList extends ItemGroup {
  public static final String ID_FOR_LISTS = "reasonable-sorting:item_group_list";
  public static final BiMap<ItemGroupList, ImmutableCollection<ItemGroup>> LIST_TO_COLLECTIONS =
      HashBiMap.create();

  private ItemGroupList(ImmutableCollection<ItemGroup> itemGroups) {
    super(0, ID_FOR_LISTS);
    LIST_TO_COLLECTIONS.put(this, itemGroups);
  }

  public static ItemGroupList of(ImmutableCollection<ItemGroup> collection) {
    return LIST_TO_COLLECTIONS.inverse().getOrDefault(collection, new ItemGroupList(collection));
  }

  public ImmutableCollection<ItemGroup> getItemGroups() {
    return LIST_TO_COLLECTIONS.get(this);
  }

  @Override
  public ItemStack createIcon() {
    return null;
  }

  @Override
  public Text getDisplayName() {
    MutableText text = new LiteralText("").formatted(Formatting.DARK_BLUE);
    for (Iterator<ItemGroup> iterator = getItemGroups().iterator(); iterator.hasNext(); ) {
      ItemGroup itemGroup = iterator.next();
      if (itemGroup instanceof ItemGroupList) {
        text.append(new TranslatableText("itemGroup.reasonable-sorting.multiple"));
      } else {
        text.append(itemGroup.getName());
      }
      if (iterator.hasNext()) {
        text.append(" / ");
      }
    }
    return text;
  }

  public boolean containsGroup(ItemGroup item) {
    return this.getItemGroups().contains(item);
  }
}
