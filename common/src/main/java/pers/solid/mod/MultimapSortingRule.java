package pers.solid.mod;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;

public final class MultimapSortingRule<T> implements SortingRule<T> {
  private final Multimap<T, T> multimap;

  public MultimapSortingRule(Multimap<T, T> multimap) {
    this.multimap = multimap;
  }

  @Override
  public @NotNull Iterable<T> getFollowers(T leadingObj) {
    return multimap.get(leadingObj);
  }
}
