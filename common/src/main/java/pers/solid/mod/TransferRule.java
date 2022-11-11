package pers.solid.mod;

import com.google.common.collect.Streams;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * 物品组转移规则，根据一个物品，决定它应该转移到哪个物品组。<p>
 * An item group transfer rule, to determine which group to be transferred to according to an item.
 */
@FunctionalInterface
public interface TransferRule {
  Logger LOGGER = LoggerFactory.getLogger(TransferRule.class);

  /**
   * 根据指定的物品以及已经注册的物品组转移规则，返回它转移的物品组的流。注意这个流可能产生重复元素。
   *
   * @param item 需要转移物品组的物品。
   * @return 返回物品转移后的物品组产生的流，可能为空，也有可能产生重复元素。
   */
  static Stream<ItemGroup> streamTransferredGroupOf(Item item) {
    return Internal.RULES.stream().map(TransferRuleContainer::transferRule)
        .map(transferRule -> transferRule.getTransferredGroups(item))
        .filter(Objects::nonNull)
        .flatMap(Streams::stream);
  }

  /**
   * 添加一条转移规则。
   *
   * @param rule 一个物品组转移规则。
   */
  static void addTransferRule(TransferRule rule) {
    addTransferRule(rule, null);
  }

  /**
   * 添加一条转移规则。
   *
   * @param rule 一个物品组转移规则。
   * @param name 需要添加的规则的名称，主要用于在调试中显示。
   */
  static void addTransferRule(@NotNull TransferRule rule, @Nullable String name) {
    Internal.RULES.add(new TransferRuleContainer(rule, name));
  }

  /**
   * 添加一个有条件的转移规则，只有在满足条件时，这个规则才会被应用。
   *
   * @param condition 一个条件。注意只有在转移物品组时，这个条件才会被评估，注册规则时并不会评估条件。
   * @param rule      一个物品组转移规则。
   */
  static void addConditionalTransferRule(@NotNull BooleanSupplier condition, @NotNull TransferRule rule) {
    addConditionalTransferRule(condition, rule, null);
  }

  /**
   * 添加一个有条件的转移规则，只有在满足条件时，这个规则才会被应用。
   *
   * @param condition 一个条件。注意只有在转移物品组时，这个条件才会被评估，注册规则时并不会评估条件。
   * @param rule      一个物品组转移规则。
   * @param name      需要添加的规则的名称，主要用于在调试中显示。
   */
  static void addConditionalTransferRule(@NotNull BooleanSupplier condition, @NotNull TransferRule rule, @Nullable String name) {
    addTransferRule(new ConditionalTransferRule(condition, rule), name);
  }

  /**
   * 根据特定物品，决定它应该转移到哪个物品组。可以返回 {@code null}，表示不转移。如果返回多个值，则表示转移到了多个物品组。
   *
   * @param item 需要转移物品组的物品。
   * @return 转移后的物品组，可以为 {@code null}。
   */
  @Nullable Iterable<ItemGroup> getTransferredGroups(Item item);

  @ApiStatus.Internal
  class Internal {
    @ApiStatus.Internal
    private static final
    List<TransferRuleContainer> RULES = new ArrayList<>();
  }

  /**
   * 用于存储在 {@link Internal#RULES} 中的对象，主要用于与名称一起保存。
   *
   * @param transferRule 转移规则。
   * @param name         名称。
   */
  record TransferRuleContainer(@NotNull TransferRule transferRule, @Nullable @NonNls String name) {
    @Override
    public String toString() {
      return "TransferRuleContainer[" + (name == null ? transferRule.toString() : name) + "]";
    }
  }
}
