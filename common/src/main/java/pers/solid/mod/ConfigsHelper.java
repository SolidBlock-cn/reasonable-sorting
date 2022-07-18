package pers.solid.mod;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 核查模组选项里的字符串形式配置并用于更新相应集合内容的实用类。
 */
public final class ConfigsHelper {
  private ConfigsHelper() {
  }

  /**
   * 根据字符串形式的 id 来返回一个物品组。
   *
   * @param id 字符串形式的id。若为 null，则返回 null。
   * @return 物品组。
   */
  @Contract(value = "!null -> _; null -> null", pure = true)
  static @Nullable ItemGroup getGroupFromId(String id) {
    if (id == null) return null;
    for (ItemGroup group : ItemGroup.GROUPS) {
      if (id.equals(group.getName())) {
        return group;
      }
    }
    return null;
  }

  @Contract(mutates = "param2")
  public static void updateVariantsFollowingBaseBlocks(String s, List<BlockFamily.Variant> mutableList) {
    mutableList.clear();
    Arrays.stream(s.split("\\s+"))
        .filter(name -> !name.isEmpty())
        .map(Configs.NAME_TO_VARIANT::get)
        .filter(Objects::nonNull)
        .forEach(Configs.VARIANTS_FOLLOWING_BASE_BLOCKS::add);
  }

  @Contract(mutates = "param2")
  public static void updateCustomSortingRules(List<String> list, Multimap<Item, Item> mutableMap) {
    mutableMap.clear();
    for (String s : list) {
      final ArrayList<String> split = Lists.newArrayList(s.split("\\s+"));
      if (split.size() < 1) {
        continue;
      }
      Identifier key = Identifier.tryParse(split.remove(0));
      if (key == null) {
        continue;
      }
      if (Bridge.itemIdExists(key)) {
        Configs.CUSTOM_ITEM_SORTING_RULES.putAll(
            Bridge.getItemById(key),
            split.stream()
                .map(Identifier::tryParse)
                .filter(Bridge::itemIdExists)
                .map(Bridge::getItemById)
                .toList());
      }
    }
  }

  @Contract(mutates = "param2")
  public static void updateCustomRegexTransferRules(List<String> list, Multimap<Pattern, ItemGroup> mutableMap) {
    mutableMap.clear();
    for (String s : list) {
      try {
        final String[] split1 = s.split("\\s+", 2);
        if (split1.length < 2) {
          continue;
        }
        final String[] split2 = split1[1].split("\\s+");
        final Pattern compile = Pattern.compile(split1[0]);
        Arrays.stream(split2).map(ConfigsHelper::getGroupFromId).filter(Objects::nonNull).forEach(group -> Configs.CUSTOM_REGEX_TRANSFER_RULE.put(compile, group));
      } catch (PatternSyntaxException ignored) {
      }
    }
  }

  @Contract(mutates = "param2")
  public static void updateCustomVariantTransferRules(List<String> list, Multimap<BlockFamily.Variant, ItemGroup> mutableMap) {
    mutableMap.clear();
    for (String s : list) {
      final String[] split1 = s.split("\\s+", 2);
      if (split1.length < 2) {
        continue;
      }
      final String[] split2 = split1[1].split("\\s+");
      final BlockFamily.Variant variant = Configs.NAME_TO_VARIANT.get(split1[0]);
      if (variant == null) continue;
      Arrays.stream(split2).map(ConfigsHelper::getGroupFromId).filter(Objects::nonNull).forEach(group -> Configs.CUSTOM_VARIANT_TRANSFER_RULE.put(variant, group));

    }
  }

  @Contract(mutates = "param2")
  public static void updateCustomTransferRule(List<String> list, Multimap<Item, ItemGroup> mutableMap) {
    for (String s : list) {
      mutableMap.clear();
      final String[] split1 = s.split("\\s+", 2);
      if (split1.length < 2) {
        continue;
      }
      final String[] split2 = split1[1].split("\\s+");
      final Identifier id = Identifier.tryParse(split1[0]);
      if (!Bridge.itemIdExists(id)) {
        continue;
      }
      final Item item = Bridge.getItemById(id);
      Arrays.stream(split2).map(ConfigsHelper::getGroupFromId).filter(Objects::nonNull).forEach(group -> mutableMap.put(item, group));
    }
  }

  @Contract(pure = true)
  public static Optional<Text> validateCustomSortingRule(String s) {
    final String[] split = s.split("\\s+");
    final List<String> invalids =
        Arrays.stream(split)
            .filter(StringUtils::isNotEmpty)
            .filter(s1 -> Identifier.tryParse(s1) == null)
            .toList();
    return !invalids.isEmpty()
        ? Optional.of(new TranslatableText("option.reasonable-sorting.error.invalid_identifier",
        String.join(" ", invalids)))
        : Optional.empty();
  }

  @Contract(pure = true)
  public static Optional<Text> validateVariantFollowsBaseBlocks(String s) {
    List<String> invalidNames = new ArrayList<>();
    Arrays.stream(s.split("\\s+"))
        .filter(StringUtils::isNotEmpty)
        .filter(name -> !Configs.NAME_TO_VARIANT.containsKey(name))
        .forEach(invalidNames::add);
    return invalidNames.isEmpty()
        ? Optional.empty()
        : Optional.of(
        new TranslatableText(
            "option.reasonable-sorting.error.invalid_variant_name",
            String.join(", ", invalidNames)));
  }

  @Contract(pure = true)
  public static Optional<Text> validateShapeFollowsBaseBlocks(String s) {
    if ("*".equals(s)) {
      return Optional.empty();
    }
    final List<String> invalids =
        Arrays.stream(s.split("\\s+"))
            .filter(StringUtils::isNotEmpty)
            .filter(s2 -> !ExtShapeBridge.INSTANCE.isValidShapeName(s2))
            .toList();
    return invalids.isEmpty()
        ? Optional.empty()
        : Optional.of(
        new TranslatableText(
            "option.reasonable-sorting.error.invalid_shape_name",
            String.join(" ", invalids)));
  }

  @Contract(pure = true)
  public static Optional<Text> validateCustomTransferRule(String s) {
    final String[] split1 = s.split("\\s+", 2);
    if (split1.length < 2) {
      return Optional.of(
          new TranslatableText(
              "option.reasonable-sorting.error.group_name_expected"));
    }
    if (Identifier.tryParse(split1[0]) == null) {
      return Optional.of(
          new TranslatableText(
              "option.reasonable-sorting.error.invalid_identifier", split1[0]));
    }
    return Optional.empty();
  }

  @Contract(pure = true)
  public static Optional<Text> validateCustomVariantTransferRule(String s) {
    if (s.isEmpty()) {
      return Optional.empty();
    }
    final String[] split1 = s.split("\\s+");
    if (split1.length < 2) {
      return Optional.of(
          new TranslatableText(
              "option.reasonable-sorting.error.group_name_expected"));
    }
    if (!Configs.NAME_TO_VARIANT.containsKey(split1[0])) {
      return Optional.of(new TranslatableText(
          "option.reasonable-sorting.error.invalid_variant_name", split1[0]));
    }
    return Optional.empty();
  }

  @Contract(pure = true)
  public static Optional<Text> validateCustomRegexTransferRule(String s) {
    if (s.isEmpty()) {
      return Optional.empty();
    }
    final String[] split = s.split("\\s+");
    if (split.length < 2) {
      return Optional.of(new TranslatableText(
          "option.reasonable-sorting.error.group_name_expected"));
    }
    final String pattern = split[0];
    try {
      Pattern.compile(pattern);
    } catch (PatternSyntaxException e) {
      final int index = e.getIndex();
      final MutableText msg = new TranslatableText(
          "option.reasonable-sorting.error.invalid_regex",
          e.getDescription(),
          new LiteralText("")
              .append(pattern.substring(0, index))
              .append("»")
              .append(new LiteralText(
                  index < pattern.length()
                      ? pattern.substring(index, index + 1)
                      : "").formatted(Formatting.DARK_RED))
              .append("«")
              .append(new LiteralText(
                  index + 1 < pattern.length()
                      ? pattern.substring(index + 1)
                      : "")));
      return Optional.of(msg);
    }
    return Optional.empty();
  }

  @Contract(pure = true)
  public static Optional<Text> validateCustomShapeTransferRule(String s) {
    if (s.isEmpty()) {
      return Optional.empty();
    }
    final String[] split = s.split("\\s+");
    if (split.length < 2) {
      return Optional.of(
          new TranslatableText(
              "option.reasonable-sorting.error.group_name_expected"));
    }
    if (!ExtShapeBridge.INSTANCE.isValidShapeName(split[0])) {
      return Optional.of(
          new TranslatableText(
              "option.reasonable-sorting.error.invalid_shape_name", split[0]));
    }
    return Optional.empty();
  }
}
