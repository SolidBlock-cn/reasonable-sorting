package pers.solid.mod;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * 此模组的配置储存。本模组使用 Auto Config 进行配置文件的读取与保存，但是配置界面仍是通过 Cloth Config 进行手动创建的。
 *
 * @author SolidBlock
 * @see ConfigScreen
 */
@Config(name = "reasonable-sorting")
public class Configs implements ConfigData {
  public static final ConfigHolder<Configs> CONFIG_HOLDER =
      AutoConfig.register(Configs.class, GsonConfigSerializer::new);
  public static final Map<String, BlockFamily.Variant> NAME_TO_VARIANT =
      Arrays.stream(BlockFamily.Variant.values())
          .collect(Collectors.toMap(BlockFamily.Variant::getName, variant -> variant));
  public static final Map<Item, Collection<Item>> CUSTOM_SORTING_RULES = new HashMap<>();
  public static final Map<Item, ItemGroup> CUSTOM_TRANSFER_RULE = new LinkedHashMap<>();
  public static final Map<BlockFamily.Variant, ItemGroup> CUSTOM_VARIANT_TRANSFER_RULE =
      new LinkedHashMap<>();
  public static final Map<Item, ItemGroup> ABSTRACT_CUSTOM_VARIANT_TRANSFER_RULE =
      new AbstractMap<>() {
        @Override
        public Set<Entry<Item, ItemGroup>> entrySet() {
          return (Registry.ITEM.stream()
              .map(item -> new SimpleImmutableEntry<>(item, get(item)))
              .filter(entry -> Objects.nonNull(entry.getValue()))
              .collect(Collectors.toUnmodifiableSet()));
        }

        @Override
        public ItemGroup get(Object key) {
          if (!(key instanceof BlockItem)) {
            return null;
          }
          for (BlockFamily blockFamily : BlockFamilyRule.BASE_BLOCKS_TO_FAMILIES.values()) {
            for (Entry<BlockFamily.Variant, ItemGroup> entry :
                CUSTOM_VARIANT_TRANSFER_RULE.entrySet()) {
              final BlockFamily.Variant variant = entry.getKey();
              if (blockFamily.getVariant(variant) == ((BlockItem) key).getBlock()) {
                return entry.getValue();
              }
            }
          }
          return null;
        }

        @Override
        public boolean containsKey(Object key) {
          return get(key) == null;
        }
      };
  public static final Map<Pattern, ItemGroup> CUSTOM_REGEX_TRANSFER_RULE = new LinkedHashMap<>();
  public static final Map<Item, ItemGroup> ABSTRACT_CUSTOM_REGEX_TRANSFER_RULE =
      new AbstractMap<>() {
        @Override
        public ItemGroup get(Object key) {
          if (key instanceof Item) {
            final Identifier id = Registry.ITEM.getId((Item) key);
            if (id == Registry.ITEM.getDefaultId()) {
              return null;
            }
            final String idString = id.toString();
            for (Entry<Pattern, ItemGroup> entry : CUSTOM_REGEX_TRANSFER_RULE.entrySet()) {
              if (entry.getKey().matcher(idString).matches()) {
                return entry.getValue();
              }
            }
          }
          return null;
        }

        @Override
        @Deprecated
        public boolean containsKey(Object key) {
          return get(key) != null;
        }

        @Override
        public boolean containsValue(Object value) {
          return CUSTOM_REGEX_TRANSFER_RULE.containsValue(value);
        }

        @NotNull
        @Override
        public Set<Entry<Item, ItemGroup>> entrySet() {
          ImmutableSet.Builder<Entry<Item, ItemGroup>> builder = new ImmutableSet.Builder<>();
          for (Item item : Registry.ITEM) {
            final ItemGroup itemGroup = get(item);
            if (itemGroup != null) {
              builder.add(new SimpleImmutableEntry<>(item, itemGroup));
            }
          }
          return builder.build();
        }
      };
  /**
   *
   *
   * <h2>排序部分</h2>
   *
   * 是否启用排序。如果该项为 {@code false}，则所有的排序都会按照原版进行。
   */
  public boolean enableSorting = true;
  /**
   * 是否启用默认的物品排序规则。
   *
   * @see DefaultItemSortingRuleSupplier
   */
  public boolean enableDefaultItemSortingRules = true;
  /**
   * 自定义排序规则。每行一条规则，由多个物品的命名空间 id 组成，用空格隔开。<br>
   * 例如：{@code grass_block dirt dirt_path} 表示将草方块、泥土、土径排在一起，以草方块的位置为准。
   *
   * @see Configs#CUSTOM_SORTING_RULES
   * @see Configs#updateCustomSortingRules
   */
  public List<String> customSortingRules = new ArrayList<>();
  /**
   * 受排序规则影响的方块变种。这些变种中的方块物品都会排在其基础方块之后。每次保存、修改配置时，都会对 {@link BlockFamilyRule#AFFECTED_VARIANTS
   * AFFECTED_VARIANTS} 进行重写。
   *
   * @see BlockFamilyRule#AFFECTED_VARIANTS
   * @see Configs#updateVariantsFollowingBaseBlocks
   */
  public String variantsFollowingBaseBlocks = "stairs slab";
  /** 受排序规则影响的 ExtShape 模组中的方块变种。 */
  public String shapesFollowingBaseBlocks = "*";
  /**
   * 栅栏门紧随栅栏。需要注意的是，该项需要和 {@link #fenceGatesInDecorations} 搭配使用。
   *
   * @see BlockFamilyRule#FENCE_TO_FENCE_GATES
   */
  public boolean fenceGateFollowsFence = true;
  /**
   *
   *
   * <h2>物品组转移部分</h2>
   *
   * 是否启用物品组转移。如果该项为 <code>false</code>，则所有的物品组转移都会按照原版进行。
   */
  public boolean enableGroupTransfer = true;
  /**
   * 按钮移至装饰性方块。
   *
   * @see ItemGroupTransfer#DEFAULT_TRANSFER_RULES
   */
  public boolean buttonsInDecorations = false;
  /**
   * 栅栏门移至装饰性方块。
   *
   * @see ItemGroupTransfer#DEFAULT_TRANSFER_RULES
   */
  public boolean fenceGatesInDecorations = true;
  /**
   * 栅栏门移至工具。
   *
   * @see ItemGroupTransfer#DEFAULT_TRANSFER_RULES
   */
  public boolean swordsInTools = false;
  /**
   * 门移至装饰性方块。
   *
   * @see ItemGroupTransfer#DEFAULT_TRANSFER_RULES
   */
  public boolean doorsInDecorations = false;
  /**
   * 自定义物品转移规则。
   *
   * @see Configs#CUSTOM_TRANSFER_RULE
   * @see Configs#updateCustomTransferRule
   */
  public List<String> transferRules = new ArrayList<>();
  /**
   * 自定义物品变种转移规则。
   *
   * @see Configs#CUSTOM_VARIANT_TRANSFER_RULE
   * @see Configs#updateCustomVariantTransferRules
   */
  public List<String> variantTransferRules = new ArrayList<>();
  /**
   * 自定义正则表达式转移规则。
   *
   * @see Configs#CUSTOM_REGEX_TRANSFER_RULE
   * @see Configs#updateCustomRegexTransferRules
   */
  public List<String> regexTransferRules = new ArrayList<>();
  /** 用于 Extended Block Shapes 模组。 */
  public List<String> shapeTransferRules = new ArrayList<>();
  /** 将蜜脾、菌光体等基础方块移至建筑方块 */
  public boolean baseBlocksInBuildingBlocks = true;

  public static Collection<Map<Item, Collection<Item>>> getCustomSortingRules() {
    return Collections.singleton(CUSTOM_SORTING_RULES);
  }

  public static Collection<Map<Item, ItemGroup>> getCustomTransferRules() {
    return ImmutableList.of(
        CUSTOM_TRANSFER_RULE,
        ABSTRACT_CUSTOM_VARIANT_TRANSFER_RULE,
        ABSTRACT_CUSTOM_REGEX_TRANSFER_RULE);
  }

  private static @Nullable ItemGroup getGroupFromId(String id) {
    for (ItemGroup group : ItemGroup.GROUPS) {
      if (Objects.equals(group.getName(), id)) {
        return group;
      }
    }
    return null;
  }

  public static void updateVariantsFollowingBaseBlocks(
      String s, List<BlockFamily.Variant> mutableList) {
    mutableList.clear();
    Arrays.stream(s.split("\\s+"))
        .filter(name -> !name.isEmpty())
        .map(NAME_TO_VARIANT::get)
        .filter(Objects::nonNull)
        .forEach(BlockFamilyRule.AFFECTED_VARIANTS::add);
  }

  public static void updateCustomSortingRules(
      List<String> list, Map<Item, Collection<Item>> mutableMap) {
    mutableMap.clear();
    for (String s : list) {
      final var split = new ArrayList<>(Arrays.asList(s.split("\\s+")));
      if (split.size() < 1) {
        continue;
      }
      var key = Identifier.tryParse(split.remove(0));
      if (key == null) {
        continue;
      }
      if (Registry.ITEM.containsId(key)) {
        CUSTOM_SORTING_RULES.put(
            Registry.ITEM.get(key),
            (split.stream()
                .map(Identifier::tryParse)
                .filter(Registry.ITEM::containsId)
                .map(Registry.ITEM::get)
                .toList()));
      }
    }
  }

  public static void updateCustomRegexTransferRules(
      List<String> list, Map<Pattern, ItemGroup> mutableMap) {
    mutableMap.clear();
    for (String s : list) {
      try {
        final String[] split = s.split("\\s+");
        if (split.length < 2) {
          continue;
        }
        final Pattern compile = Pattern.compile(split[0]);
        final ItemGroup group = getGroupFromId(split[1]);
        CUSTOM_REGEX_TRANSFER_RULE.put(
            Objects.requireNonNull(compile), Objects.requireNonNull(group));
      } catch (NullPointerException | PatternSyntaxException ignored) {
      }
    }
  }

  public static void updateCustomVariantTransferRules(
      List<String> list, Map<BlockFamily.Variant, ItemGroup> mutableMap) {
    mutableMap.clear();
    for (String s : list) {
      try {
        final String[] split = s.split("\\s+");
        if (split.length < 2) {
          continue;
        }
        final BlockFamily.Variant variant = NAME_TO_VARIANT.get(split[0]);
        final ItemGroup group = getGroupFromId(split[1]);
        CUSTOM_VARIANT_TRANSFER_RULE.put(
            Objects.requireNonNull(variant), Objects.requireNonNull(group));
      } catch (NullPointerException ignored) {
      }
    }
  }

  public static void updateCustomTransferRule(List<String> list, Map<Item, ItemGroup> mutableMap) {
    for (String s : list) {
      mutableMap.clear();
      final String[] split = s.split("\\s+");
      if (split.length < 2) {
        continue;
      }
      final Identifier id = Identifier.tryParse(split[0]);
      if (!Registry.ITEM.containsId(id)) {
        continue;
      }
      final Item item = Registry.ITEM.get(id);
      ItemGroup itemGroup = getGroupFromId(split[1]);
      mutableMap.put(item, itemGroup);
    }
  }
}
