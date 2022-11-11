package pers.solid.mod;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class ConditionalSortingRule<T> implements SortingRule<T> {
  private final BooleanSupplier condition;
  private final SortingRule<T> sortingRule;

  ConditionalSortingRule(BooleanSupplier condition, SortingRule<T> sortingRule) {
    this.condition = condition;
    this.sortingRule = sortingRule;
  }

  public BooleanSupplier condition() {
    return condition;
  }

  public SortingRule<T> sortingRule() {
    return sortingRule;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    ConditionalSortingRule<?> that = (ConditionalSortingRule<?>) obj;
    return Objects.equals(this.condition, that.condition) &&
        Objects.equals(this.sortingRule, that.sortingRule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(condition, sortingRule);
  }

  @Override
  public String toString() {
    return "ConditionalSortingRule[" +
        "condition=" + condition + ", " +
        "sortingRule=" + sortingRule + ']';
  }

  @Override
  public @Nullable Iterable<T> getFollowers(T leadingObj) {
    return condition.getAsBoolean() ? sortingRule.getFollowers(leadingObj) : null;
  }
}
