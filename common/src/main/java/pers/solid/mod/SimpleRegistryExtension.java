package pers.solid.mod;

import com.google.common.base.Predicates;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;

/**
 * @see pers.solid.mod.mixin.SimpleRegistryMixin
 */
public interface SimpleRegistryExtension {
  static void removeAllCachedEntries() {
    if (Configs.instance.sortingCalculationType == SortingCalculationType.STANDARD) {
      Registry.REGISTRIES.stream().filter(Predicates.instanceOf(SimpleRegistryExtension.class)).forEach(r -> ((SimpleRegistryExtension) r).removeCachedEntries());
      SortingRule.Internal.cachedInventoryItems = null;
    }
  }

  /**
   * 移除注册表的 {@code cachedEntries} 字段，使得在下次运行 {@code getEntries} 时重新迭代其内容。
   */
  @Contract(mutates = "this")
  void removeCachedEntries();
}
