package pers.solid.mod.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingCalculationType;
import pers.solid.mod.SortingInfluenceRange;
import pers.solid.mod.SortingRule;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {
  @Shadow
  public abstract String getName();

  @Environment(EnvType.CLIENT)
  @ModifyVariable(method = "appendStacks", at = @At("STORE"))
  public Iterator<Item> modifiedAppendStacks(Iterator<Item> value) {
    if (Configs.instance.enableSorting && Configs.instance.sortingInfluenceRange == SortingInfluenceRange.INVENTORY_ONLY) {
      if (Configs.instance.sortingCalculationType != SortingCalculationType.STANDARD || SortingRule.Internal.cachedInventoryItems == null) {
        if (Configs.instance.debugMode) {
          SortingRule.LOGGER.info("Calculating the sorting in the creative inventory or {}. It may cause a slight lag, but will no longer happen until you modify configs.", getName());
        }
        // 如果排序计算类型为实时或者半实时，或者计算类型为标准但是 cachedInventoryItems 为 null，那么迭代一次其中的内容。
        final Stream<Item> stream = SortingRule.streamOfRegistry(Registry.ITEM_KEY, Lists.newArrayList(value));
        if (Configs.instance.sortingCalculationType == SortingCalculationType.STANDARD) {
          // 如果为 standard，保存这次迭代的结果，下次直接使用。
          final List<Item> list = stream == null ? ImmutableList.copyOf(value) : stream.collect(Collectors.toList());
          SortingRule.Internal.cachedInventoryItems = list;
          return list.iterator();
        } else {
          // 如果为 real-time 或 semi-real-time，则直接返回这个流的迭代器，或者原来的迭代器。
          return stream == null ? value : stream.iterator();
        }
      } else {
        if (Configs.instance.debugMode) {
          SortingRule.LOGGER.info("During the iteration in the creative inventory {}, the cached item list is still used, because the 'sorting calculation type' is set to 'standard'.", getName());
        }
        return SortingRule.Internal.cachedInventoryItems.iterator();
      }
    }
    return value;
  }
}
