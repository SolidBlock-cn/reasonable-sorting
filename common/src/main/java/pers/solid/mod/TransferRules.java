package pers.solid.mod;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Identifier;
import pers.solid.mod.mixin.BlockFamiliesAccessor;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * 本模组内置的一些物品组转移规则。
 */
public final class TransferRules {
  /**
   * 将原本位于“红石”中的按钮转移至“装饰性方块”。
   */
  public static final TransferRule BUTTON_IN_DECORATIONS = item -> item.getGroup() == ItemGroup.REDSTONE && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractButtonBlock ? Collections.singleton(ItemGroup.DECORATIONS) : null;
  /**
   * 将原本位于“红石”中的栅栏门转移至“装饰性方块”。
   */
  public static final TransferRule FENCE_GATE_IN_DECORATIONS = item -> item.getGroup() == ItemGroup.REDSTONE && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof FenceGateBlock ? Collections.singleton(ItemGroup.DECORATIONS) : null;
  /**
   * 将原本位于“红石”中的门转移至“装饰性方块”。
   */
  public static final TransferRule DOORS_IN_DECORATIONS = item -> item.getGroup() == ItemGroup.REDSTONE && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof DoorBlock ? Collections.singleton(ItemGroup.DECORATIONS) : null;
  /**
   * 将原本位于“工具”中的剑转移至“装饰性方块”。
   */
  public static final TransferRule SWORDS_IN_TOOLS = item -> item instanceof SwordItem && item.getGroup() == ItemGroup.COMBAT ? Collections.singleton(ItemGroup.TOOLS) : null;
  /**
   * 自定义的变种转移规则。仅限方块物品。
   */
  public static final TransferRule CUSTOM_VARIANT_TRANSFER_RULE = item -> {
    if (!(item instanceof BlockItem blockItem)) return null;
    final Block block = blockItem.getBlock();
    BlockFamily.Variant variant = null;
    final BlockFamily.Variant[] variants = BlockFamily.Variant.values();
    loop1:
    for (BlockFamily blockFamily : BlockFamiliesAccessor.getBaseBlocksToFamilies().values()) {
      for (BlockFamily.Variant variant1 : variants) {
        if (blockFamily.getVariant(variant1) == block) {
          variant = variant1;
          break loop1;
        }
      }
    }
    return variant == null ? null : Configs.CUSTOM_VARIANT_TRANSFER_RULE.get(variant);
  };
  /**
   * 自定义的正则表达式转移规则。
   */
  public static final TransferRule CUSTOM_REGEX_TRANSFER_RULE = item -> {
    final Identifier identifier = Bridge.getItemId(item);
    for (final Pattern pattern : Configs.CUSTOM_REGEX_TRANSFER_RULE.keySet()) {
      if (pattern.matcher(identifier.toString()).matches()) return Configs.CUSTOM_REGEX_TRANSFER_RULE.get(pattern);
    }
    return null;
  };

  private TransferRules() {
  }

  static void initialize() {
    TransferRule.addConditionalTransferRule(() -> Configs.instance.buttonsInDecorations, BUTTON_IN_DECORATIONS);
    TransferRule.addConditionalTransferRule(() -> Configs.instance.fenceGatesInDecorations, FENCE_GATE_IN_DECORATIONS);
    TransferRule.addConditionalTransferRule(() -> Configs.instance.doorsInDecorations, DOORS_IN_DECORATIONS);
    TransferRule.addConditionalTransferRule(() -> Configs.instance.swordsInTools, SWORDS_IN_TOOLS);
    TransferRule.addTransferRule(Configs.CUSTOM_TRANSFER_RULE::get);
    TransferRule.addConditionalTransferRule(() -> !Configs.CUSTOM_VARIANT_TRANSFER_RULE.isEmpty(), CUSTOM_VARIANT_TRANSFER_RULE);
    TransferRule.addConditionalTransferRule(() -> !Configs.CUSTOM_REGEX_TRANSFER_RULE.isEmpty(), CUSTOM_REGEX_TRANSFER_RULE);
  }
}
