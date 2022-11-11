package pers.solid.mod;

import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public record ConditionalSortingRule<T>(BooleanSupplier condition, SortingRule<T> sortingRule) implements SortingRule<T> {
  @Override
  public @Nullable Iterable<T> getFollowers(T leadingObj) {
    return condition.getAsBoolean() ? sortingRule.getFollowers(leadingObj) : null;
  }
}
