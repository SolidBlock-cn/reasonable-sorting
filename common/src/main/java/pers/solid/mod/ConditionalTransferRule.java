package pers.solid.mod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class ConditionalTransferRule implements TransferRule {
  private final @NotNull BooleanSupplier condition;
  private final @NotNull TransferRule transferRule;

  public ConditionalTransferRule(@NotNull BooleanSupplier condition, @NotNull TransferRule transferRule) {
    this.condition = condition;
    this.transferRule = transferRule;
  }

  public BooleanSupplier condition() {
    return condition;
  }

  public TransferRule transferRule() {
    return transferRule;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    ConditionalTransferRule that = (ConditionalTransferRule) obj;
    return Objects.equals(this.condition, that.condition) &&
        Objects.equals(this.transferRule, that.transferRule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(condition, transferRule);
  }

  @Override
  public String toString() {
    return "ConditionalTransferRule[" +
        "condition=" + condition + ", " +
        "transferRule=" + transferRule + ']';
  }

  @Override
  public @Nullable Iterable<ItemGroup> getTransferredGroups(Item item) {
    return condition.getAsBoolean() ? transferRule.getTransferredGroups(item) : null;
  }
}
