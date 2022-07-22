package pers.solid.mod;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.event.ConfigSerializeEvent;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 此模组的配置储存。考虑到模组在不同的平台对配置的使用有所不同，因此不同平台的模组继承此类，并各自替换 {@link #instance} 字段。本模组使用 Auto Config 进行配置文件的读取与保存，但是配置界面仍是通过 Cloth Config 进行手动创建的。
 */
@Config(name = "reasonable-sorting")
public class Configs implements ConfigData {
  /**
   * 由方块变种名称到方块变种的不可变映射。模组配置时，可能需要使用方块变种的名称。
   */
  public static final @Unmodifiable ImmutableMap<String, BlockFamily.Variant> NAME_TO_VARIANT = Arrays.stream(BlockFamily.Variant.values()).collect(ImmutableMap.toImmutableMap(BlockFamily.Variant::getName, Functions.identity()));
  /**
   * 自定义的方块排序规则，目前暂未使用。<p>
   * 注意这个字段是静态的，当保存配置时，直接修改这个 Multimap 的内容。
   */
  public static final Multimap<Block, Block> CUSTOM_BLOCK_SORTING_RULES = LinkedListMultimap.create();
  /**
   * 自定义的物品排序规则。这个字段是静态的，当保存配置时，会根据保存的对应配置项（一般是字符串列表）更新这个 Multimap 的内容。
   *
   * @see #customSortingRules
   */
  public static final Multimap<Item, Item> CUSTOM_ITEM_SORTING_RULES = LinkedListMultimap.create();
  /**
   * 自定义的物品组转移规则。这个字段是静态的，当保存配置时，会根据保存的对应配置项（一般是字符串列表）更新这个 Multimap 的内容。
   *
   * @see #transferRules
   */
  public static final Multimap<Item, ItemGroup> CUSTOM_TRANSFER_RULE = LinkedListMultimap.create();
  /**
   * 自定义的方块变种的物品组转移规则，只影响方块物品。这个字段是静态的，当保存配置时，会根据保存的对应配置项（一般是字符串列表）更新这个 Multimap 的内容。
   *
   * @see #variantTransferRules
   */
  public static final Multimap<BlockFamily.Variant, ItemGroup> CUSTOM_VARIANT_TRANSFER_RULE = LinkedListMultimap.create();
  /**
   * 自定义正则表达式的物品组转移规则。这个字段是静态的，当保存配置时，会根据保存的对应配置项（一般是字符串列表）更新这个 Multimap 的内容。
   *
   * @see #regexTransferRules
   */
  public static final Multimap<Pattern, ItemGroup> CUSTOM_REGEX_TRANSFER_RULE = LinkedListMultimap.create();
  /**
   * 对该数组内的变种应用排序，其他变种不受影响。这个字段是静态的，当保存配置时，会根据保存的对应配置项（一般是字符串列表）更新这个 Multimap 的内容。<p>
   * 本字段应当与 {@link Configs#variantsFollowingBaseBlocks} 定义的默认值一致。
   *
   * @see #variantsFollowingBaseBlocks
   */
  public static final ArrayList<BlockFamily.Variant> VARIANTS_FOLLOWING_BASE_BLOCKS = Lists.newArrayList(BlockFamily.Variant.STAIRS, BlockFamily.Variant.SLAB);
  public static final ConfigHolder<Configs> CONFIG_HOLDER = AutoConfig.register(Configs.class, GsonConfigSerializer::new);

  /*

  ===== SORTING PART =====

   */

  /**
   * <h2>排序部分</h2>
   * <p>
   * 是否启用排序。如果该项为 {@code false}，则所有的排序都会按照原版进行。
   */
  public boolean enableSorting = true;
  /**
   * 是否启用默认的物品排序规则。
   *
   * @see SortingRules#DEFAULT_ITEM_SORTING_RULE
   */
  public boolean enableDefaultItemSortingRules = true;
  /**
   * 自定义排序规则。每行一条规则，由多个物品的命名空间 id 组成，用空格隔开。<p>
   * 例如：{@code grass_block dirt dirt_path} 表示将草方块、泥土、土径排在一起，以草方块的位置为准。<p>
   * 注意：在实际排序时，不是取决于此字段，而是取决于 {@link #CUSTOM_BLOCK_SORTING_RULES}，因此加载和保存配置时，应进行更新。
   *
   * @see #CUSTOM_BLOCK_SORTING_RULES
   * @see ConfigsHelper#updateCustomSortingRules
   */
  public List<String> customSortingRules = new ArrayList<>();

  /**
   * 受排序规则影响的方块变种。这些变种中的方块物品都会排在其基础方块之后。每次保存、修改配置时，都会对 {@link #VARIANTS_FOLLOWING_BASE_BLOCKS} 进行重写。<p>
   * 注意：在实际排序时，不是取决于此字段，而是取决于 {@link #VARIANTS_FOLLOWING_BASE_BLOCKS}，因此加载和保存配置时，应进行更新。
   *
   * @see #VARIANTS_FOLLOWING_BASE_BLOCKS
   * @see ConfigsHelper#updateVariantsFollowingBaseBlocks
   */
  public String variantsFollowingBaseBlocks = "stairs slab";
  /**
   * 受排序规则影响的 ExtShape 模组中的方块变种。本字段内容在 Extended Block Shapes 模组中进行读取，未安装该模组时，本字段仍会正常加载和保存，但不会显示在模组配置屏幕中。
   */
  public String shapesFollowingBaseBlocks = "*";
  /**
   * 栅栏门紧随栅栏。需要注意的是，该项需要和 {@link #fenceGatesInDecorations} 搭配使用。
   *
   * @see SortingRules#FENCE_GATE_FOLLOWS_FENCE_ITEM
   */
  public boolean fenceGateFollowsFence = true;
  /**
   * 若开启，则上述规则只影响物品（包括物品形式的方块），不影响方块，也就是说，调试模式下的所有方块仍按照原版方式排序，但是创造模式物品栏里面的方块则依然受影响。
   */
  public boolean blockItemsOnly = false;
  
  /*
  
  ===== TRANSFER PART =====
  
   */

  /**
   * <h2>物品组转移部分</h2>
   * <p>
   * 是否启用物品组转移。如果该项为 <code>false</code>，则所有的物品组转移都会按照原版进行。
   */
  public boolean enableGroupTransfer = true;
  /**
   * 按钮移至装饰性方块。
   *
   * @see TransferRules#BUTTON_IN_DECORATIONS
   */
  public boolean buttonsInDecorations = false;
  /**
   * 栅栏门移至装饰性方块。
   *
   * @see TransferRules#FENCE_GATE_IN_DECORATIONS
   */
  public boolean fenceGatesInDecorations = true;
  /**
   * 剑移至工具。
   *
   * @see TransferRules#SWORDS_IN_TOOLS
   */
  public boolean swordsInTools = false;
  /**
   * 门移至装饰性方块。
   *
   * @see TransferRules#DOORS_IN_DECORATIONS
   */
  public boolean doorsInDecorations = false;
  /**
   * 自定义物品转移规则。注意：在实际排序时，不是取决于此字段，而是取决于 {@link #CUSTOM_TRANSFER_RULE}，因此加载和保存配置时，应根据此字段内容进行修改。
   *
   * @see #CUSTOM_TRANSFER_RULE
   * @see ConfigsHelper#updateCustomTransferRule
   */
  public List<String> transferRules = new ArrayList<>();
  /**
   * 自定义物品变种转移规则。注意：在实际排序时，不是取决于此字段，而是取决于 {@link #CUSTOM_VARIANT_TRANSFER_RULE}，因此加载和保存配置时，应根据此字段内容进行修改。
   *
   * @see #CUSTOM_VARIANT_TRANSFER_RULE
   * @see ConfigsHelper#updateCustomVariantTransferRules
   */
  public List<String> variantTransferRules = new ArrayList<>();
  /**
   * 自定义正则表达式转移规则。注意：在实际排序时，不是取决于此字段，而是取决于 {@link #CUSTOM_REGEX_TRANSFER_RULE}，因此加载和保存配置时，应根据此字段内容进行修改。
   *
   * @see #CUSTOM_REGEX_TRANSFER_RULE
   * @see ConfigsHelper#updateCustomRegexTransferRules
   */
  public List<String> regexTransferRules = new ArrayList<>();
  /**
   * 用于 Extended Block Shapes 模组。没有安装此模组时，此字段仍会正常加载和保存，但是不会显示在配置屏幕中。
   */
  public List<String> shapeTransferRules = new ArrayList<>();
  /**
   * 用于 Extended Block Shapes 模组，将蜜脾、菌光体等基础方块移至建筑方块。没有安装此模组时，此字段仍会正常加载和保存，但是不会显示在配置屏幕中。
   */
  public boolean baseBlocksInBuildingBlocks = true;
  private static final Logger LOGGER = LoggerFactory.getLogger("ReasonableSorting Configs");
  /**
   * 这个配置的实例。通常这个值应该为 {@link ConfigHolder#getConfig()} 返回的结果。当被修改时，这个字段也相应修改。初始值为新的实例，然后再会在 {@link #loadAndUpdate()} 中替换。
   */
  public static Configs instance = new Configs();

  public static void loadAndUpdate() {
    // 初始化时先手动设置，之后在每次更新和保存时，都会在 listener 中自动更新这个字段的值。
    Configs.instance = CONFIG_HOLDER.getConfig();

    final ConfigSerializeEvent.Load<Configs> update = (configHolder, configs) -> {
      ConfigsHelper.updateCustomSortingRules(configs.customSortingRules, Configs.CUSTOM_ITEM_SORTING_RULES);
      ConfigsHelper.updateCustomTransferRule(configs.transferRules, Configs.CUSTOM_TRANSFER_RULE);
      ConfigsHelper.updateCustomRegexTransferRules(configs.regexTransferRules, Configs.CUSTOM_REGEX_TRANSFER_RULE);
      ConfigsHelper.updateCustomVariantTransferRules(configs.variantTransferRules, Configs.CUSTOM_VARIANT_TRANSFER_RULE);
      ConfigsHelper.updateVariantsFollowingBaseBlocks(configs.variantsFollowingBaseBlocks, Configs.VARIANTS_FOLLOWING_BASE_BLOCKS);
      ExtShapeBridge.INSTANCE.updateShapeList(configs.shapesFollowingBaseBlocks);
      ExtShapeBridge.INSTANCE.updateShapeTransferRules(configs.shapeTransferRules);
      return ActionResult.PASS;
    };
    CONFIG_HOLDER.registerLoadListener((configHolder, configs) -> {
      Configs.instance = configs;
      LOGGER.info("Loading Reasonable Sorting configs.");
      update.onLoad(configHolder, configs);
      return ActionResult.SUCCESS;
    });

    // ConfigManager 在构造的时候就会调用一次 load，但是当时还是没有注册 loadListener 的，因此需要手动调用一次。
    update.onLoad(CONFIG_HOLDER, Configs.instance);

    CONFIG_HOLDER.registerSaveListener((configHolder, configs) -> {
      Configs.instance = configs;
      LOGGER.info("Saving Reasonable Sorting configs.");
      return ActionResult.SUCCESS;
    });
  }
}
