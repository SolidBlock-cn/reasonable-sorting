package pers.solid.mod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public record ConditionalTransferRule(@NotNull BooleanSupplier condition, @NotNull TransferRule transferRule) implements TransferRule {

  @Override
  public @Nullable Iterable<ItemGroup> getTransferredGroups(Item item) {
    return condition.getAsBoolean() ? transferRule.getTransferredGroups(item) : null;
  }
}
