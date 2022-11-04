package pers.solid.mod;

import BaseToVariantRule;
import VariantToVariantRule;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Unmodifiable;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;

import java.util.Collections;

/**
 * 本模组自带的一些排序规则。
 */
public final class SortingRules {

  /**
   * 此模组默认内置的方块排序规则。
   */
  public static final @Unmodifiable ImmutableMultimap<Block, Block> DEFAULT_BLOCK_SORTING_RULE = new ImmutableMultimap.Builder<Block, Block>()
      //.put(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE)
      //.putAll(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE)
      //.putAll(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE)
      //.putAll(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS)
      //.putAll(Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_PILLAR)
      //.put(Blocks.OAK_SLAB, Blocks.PETRIFIED_OAK_SLAB)
      //.put(Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB)
      .build();
  /**
   * 此模组默认内置的物品排序规则。
   */
  public static final @Unmodifiable ImmutableMultimap<Item, Item> DEFAULT_ITEM_SORTING_RULE
      = new ImmutableMultimap.Builder<Item, Item>()
      //.put(Items.COBBLESTONE, Items.MOSSY_COBBLESTONE)
      //.putAll(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE, Items.RED_SANDSTONE, Items.CHISELED_RED_SANDSTONE, Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE)
      //.putAll(Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE)
      //.putAll(Items.NETHER_BRICKS, Items.CRACKED_NETHER_BRICKS, Items.RED_NETHER_BRICKS)
      //.putAll(Items.QUARTZ_BLOCK, Items.SMOOTH_QUARTZ, Items.CHISELED_QUARTZ_BLOCK, Items.QUARTZ_BRICKS, Items.QUARTZ_PILLAR)
      //.put(Items.OAK_SLAB, Items.PETRIFIED_OAK_SLAB)
      //.put(Items.SMOOTH_STONE, Items.SMOOTH_STONE_SLAB)
      //.put(Items.BOOK, Items.WRITABLE_BOOK)
      //.put(Items.PAPER, Items.MAP)
      //.put(Items.GOLD_NUGGET, Items.IRON_NUGGET)
      //.put(Items.BRICK, Items.NETHER_BRICK)
      //.putAll(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS)
      //.putAll(Items.SNOWBALL, Items.CLAY_BALL, Items.ENDER_PEARL, Items.ENDER_EYE)
      //.put(Items.BOW, Items.CROSSBOW)
      //.putAll(Items.ARROW, Items.TRIDENT, Items.SHIELD, Items.TOTEM_OF_UNDYING)
      //.putAll(Items.GHAST_TEAR, Items.FERMENTED_SPIDER_EYE, Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.BREWING_STAND, Items.CAULDRON, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT, Items.RABBIT_FOOT, Items.PHANTOM_MEMBRANE, Items.GLASS_BOTTLE, Items.DRAGON_BREATH)
      //.putAll(Items.FLINT, Items.SNOWBALL, Items.LEATHER)

      // due works buggy in snapshots (weapon in tools)
          .put(Items.WOODEN_SWORD, Items.WOODEN_SHOVEL).putAll(Items.WOODEN_SHOVEL, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE)
          .put(Items.STONE_SWORD, Items.STONE_SHOVEL).putAll(Items.STONE_SHOVEL, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE)
          .put(Items.IRON_SWORD, Items.IRON_SHOVEL).putAll(Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE)
          .put(Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL).putAll(Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE)
          .put(Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL).putAll(Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE)
          .put(Items.NETHERITE_SWORD, Items.NETHERITE_SHOVEL).putAll(Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE)
          .build();
  /**
   * 在 {@link Configs#VARIANTS_FOLLOWING_BASE_BLOCKS} 中的各个变种的方块，应该跟随其基础方块。
   */
  public static final BaseToVariantRule VARIANT_FOLLOWS_BASE = new BaseToVariantRule(Predicates.alwaysTrue(), Configs.VARIANTS_FOLLOWING_BASE_BLOCKS);
  /**
   * {@link #VARIANT_FOLLOWS_BASE} 对应的物品规则，影响方块物品。
   */
  public static final SortingRule<Item> VARIANT_FOLLOWS_BASE_ITEM = new BlockItemRule(VARIANT_FOLLOWS_BASE);
  /**
   * 让所有的栅栏门紧随在栅栏的后面。
   */
  public static final VariantToVariantRule FENCE_GATE_FOLLOWS_FENCE = new VariantToVariantRule(block -> block instanceof FenceBlock, BlockFamily.Variant.FENCE, Collections.singleton(BlockFamily.Variant.FENCE_GATE));
  /**
   * {@link #FENCE_GATE_FOLLOWS_FENCE} 对应的物品规则，影响方块物品。
   */
  public static final SortingRule<Item> FENCE_GATE_FOLLOWS_FENCE_ITEM = new BlockItemRule(FENCE_GATE_FOLLOWS_FENCE);

  private SortingRules() {
  }

  public static void initialize() {
    SortingRule.addSortingRule(Registry.BLOCK_KEY, Configs.CUSTOM_BLOCK_SORTING_RULES::get);
    SortingRule.addSortingRule(Registry.ITEM_KEY, Configs.CUSTOM_ITEM_SORTING_RULES::get);
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> Configs.instance.enableDefaultItemSortingRules && !Configs.instance.blockItemsOnly, DEFAULT_BLOCK_SORTING_RULE::get);
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> Configs.instance.enableDefaultItemSortingRules, DEFAULT_ITEM_SORTING_RULE::get);
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> !Configs.VARIANTS_FOLLOWING_BASE_BLOCKS.isEmpty() && !Configs.instance.blockItemsOnly, VARIANT_FOLLOWS_BASE);
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> !Configs.VARIANTS_FOLLOWING_BASE_BLOCKS.isEmpty(), VARIANT_FOLLOWS_BASE_ITEM);
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> Configs.instance.fenceGateFollowsFence && !Configs.instance.blockItemsOnly, FENCE_GATE_FOLLOWS_FENCE);
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> Configs.instance.fenceGateFollowsFence, FENCE_GATE_FOLLOWS_FENCE_ITEM);
  }
}
