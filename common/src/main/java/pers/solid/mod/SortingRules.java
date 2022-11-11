package pers.solid.mod;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;

/**
 * 本模组自带的一些排序规则。
 */
public final class SortingRules {
  /**
   * 此模组默认内置的方块排序规则。
   */
  public static final @Unmodifiable ImmutableMultimap<Block, Block> DEFAULT_BLOCK_SORTING_RULE = new ImmutableMultimap.Builder<Block, Block>()
      .put(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE)
      .putAll(Blocks.SAND, Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.RED_SAND)
      .putAll(Blocks.RED_SAND, Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE)
      .putAll(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE)
      .putAll(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS)
      .putAll(Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_PILLAR)
      .put(Blocks.OAK_SLAB, Blocks.PETRIFIED_OAK_SLAB)
      .put(Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB)
      .put(Blocks.NETHERITE_BLOCK, Blocks.COPPER_BLOCK)
      .build();
  /**
   * 此模组默认内置的物品排序规则。
   */
  public static final @Unmodifiable ImmutableMultimap<Item, Item> DEFAULT_ITEM_SORTING_RULE
      = new ImmutableMultimap.Builder<Item, Item>()
      .putAll((Iterable<Map.Entry<Item, Item>>) DEFAULT_BLOCK_SORTING_RULE.entries().stream()
          .map(entry -> Maps.immutableEntry(entry.getKey().asItem(), entry.getValue().asItem()))
          .filter(entry -> entry.getKey() != Items.AIR && entry.getValue() != Items.AIR)
          ::iterator)
      .put(Items.TORCH, Items.SOUL_TORCH)
      .put(Items.BOOK, Items.WRITABLE_BOOK)
      .put(Items.PAPER, Items.MAP)
      .put(Items.GOLD_NUGGET, Items.IRON_NUGGET)
      .put(Items.BRICK, Items.NETHER_BRICK)
      .putAll(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS)
      .putAll(Items.SNOWBALL, Items.CLAY_BALL, Items.ENDER_PEARL, Items.ENDER_EYE)
      .put(Items.BOW, Items.CROSSBOW)
      .putAll(Items.ARROW, Items.TRIDENT, Items.SHIELD, Items.TOTEM_OF_UNDYING)
      .putAll(Items.GHAST_TEAR, Items.FERMENTED_SPIDER_EYE, Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.BREWING_STAND, Items.CAULDRON, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT, Items.RABBIT_FOOT, Items.PHANTOM_MEMBRANE, Items.GLASS_BOTTLE, Items.DRAGON_BREATH)
      .putAll(Items.FLINT, Items.SNOWBALL, Items.LEATHER)
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
  public static final ColorSortingRule<Block> COLOR_SORTING_RULE = new ColorSortingRule<>(DyeColor.WHITE, ImmutableList.of(
      DyeColor.LIGHT_GRAY,
      DyeColor.GRAY,
      DyeColor.BLACK,
      DyeColor.BROWN,
      DyeColor.RED,
      DyeColor.ORANGE,
      DyeColor.YELLOW,
      DyeColor.LIME,
      DyeColor.GREEN,
      DyeColor.CYAN,
      DyeColor.LIGHT_BLUE,
      DyeColor.BLUE,
      DyeColor.PURPLE,
      DyeColor.MAGENTA,
      DyeColor.PINK
  ));
  public static final ColorSortingRule<Item> COLOR_SORTING_RULE_ITEM = new ColorSortingRule<>(COLOR_SORTING_RULE.baseColor(), COLOR_SORTING_RULE.followingColors());

  private SortingRules() {
  }

  public static void initialize() {
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> !Configs.VARIANTS_FOLLOWING_BASE_BLOCKS.isEmpty() && !Configs.instance.blockItemsOnly, VARIANT_FOLLOWS_BASE, 8, "variant follows base");
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> !Configs.VARIANTS_FOLLOWING_BASE_BLOCKS.isEmpty(), VARIANT_FOLLOWS_BASE_ITEM, 8, "variant follows base");
    SortingRule.addSortingRule(Registry.BLOCK_KEY, new MultimapSortingRule<>(Configs.CUSTOM_BLOCK_SORTING_RULES), "custom block sorting rules");
    SortingRule.addSortingRule(Registry.ITEM_KEY, new MultimapSortingRule<>(Configs.CUSTOM_ITEM_SORTING_RULES), "custom item sorting rules");
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> Configs.instance.enableDefaultItemSortingRules && !Configs.instance.blockItemsOnly, new MultimapSortingRule<>(DEFAULT_BLOCK_SORTING_RULE), "default block sorting rule");
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> Configs.instance.enableDefaultItemSortingRules, new MultimapSortingRule<>(DEFAULT_ITEM_SORTING_RULE), "default item sorting rule");
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> Configs.instance.fenceGateFollowsFence && !Configs.instance.blockItemsOnly, FENCE_GATE_FOLLOWS_FENCE, 1, "fence gate follows fence");
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> Configs.instance.fenceGateFollowsFence, FENCE_GATE_FOLLOWS_FENCE_ITEM, 1, "fence gate follows fence");
    SortingRule.addConditionalSortingRule(Registry.BLOCK_KEY, () -> Configs.instance.fancyColorsSorting && !Configs.instance.blockItemsOnly, COLOR_SORTING_RULE, -1, "fancy colors");
    SortingRule.addConditionalSortingRule(Registry.ITEM_KEY, () -> Configs.instance.fancyColorsSorting, COLOR_SORTING_RULE_ITEM, -1, "fancy colors");

    if (Configs.instance.debugMode) {
      SortingRule.LOGGER.info("Initializing Sorting Rules. It may happen before or after the registration of mod items, but should take place before loading Reasonable Sorting Configs.");
    }
  }
}
