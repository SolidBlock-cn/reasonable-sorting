package pers.solid.mod;

import com.google.common.collect.ImmutableList;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** @author SolidBlock */
public class ConfigScreen implements ModMenuApi {

  public static List<String> toStringList(Map<Item, Collection<Item>> map) {
    List<String> list = new ArrayList<>();
    for (Map.Entry<Item, Collection<Item>> entry : map.entrySet()) {
      final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
      final Identifier id = Registry.ITEM.getId(entry.getKey());
      if (id == Registry.ITEM.getDefaultId()) {
        continue;
      }
      builder.add(id.toString());
      for (Item item : entry.getValue()) {
        final Identifier id1 = Registry.ITEM.getId(item);
        if (id1 == Registry.ITEM.getDefaultId()) {
          continue;
        }
        builder.add(id1.toString());
      }
      list.add(String.join(" ", builder.build()));
    }
    return list;
  }

  public static List<String> formatted(List<String> list) {
    List<String> newList = new ArrayList<>();
    for (String s : list) {
      newList.add(
          String.join(
              " ",
              Arrays.stream(s.split("\\s+"))
                  .map(Identifier::tryParse)
                  .filter(Objects::nonNull)
                  .map(Identifier::toString)
                  .toList()));
    }
    return newList;
  }

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return this::createScreen;
  }

  /**
   * 模组配置屏幕创建。
   *
   * @param previousScreen 上层屏幕。
   * @return 配置屏幕。
   */
  private Screen createScreen(Screen previousScreen) {
    final Configs config = Configs.CONFIG_HOLDER.getConfig();
    ConfigBuilder builder =
        ConfigBuilder.create()
            .setParentScreen(previousScreen)
            .setSavingRunnable(Configs.CONFIG_HOLDER::save)
            .setTitle(new TranslatableText("title.reasonable-sorting.config"));

    ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    ConfigCategory categorySorting =
        builder.getOrCreateCategory(new TranslatableText("category.reasonable-sorting.sorting"));
    categorySorting.setDescription(
        new TranslatableText[] {
          new TranslatableText("category.reasonable-sorting.sorting.description")
        });
    ConfigCategory categoryTransfer =
        builder.getOrCreateCategory(new TranslatableText("category.reasonable-sorting.transfer"));
    categoryTransfer.setDescription(
        new TranslatableText[] {
          new TranslatableText("category.reasonable-sorting.transfer.description")
        });

    // 排序部分。
    categorySorting.addEntry(
        entryBuilder
            .startTextDescription(
                new TranslatableText("category.reasonable-sorting.sorting.description"))
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.enable_sorting"),
                config.enableSorting)
            .setTooltip(new TranslatableText("option.reasonable-sorting.enable_sorting.tooltip"))
            .setYesNoTextSupplier(
                b ->
                    new TranslatableText(
                        b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setDefaultValue(true)
            .setSaveConsumer(b -> config.enableSorting = b)
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.enable_default_item_sorting_rules"),
                config.enableDefaultItemSortingRules)
            .setTooltip(
                new TranslatableText(
                    "option.reasonable-sorting.enable_default_item_sorting_rules.tooltip"))
            .setYesNoTextSupplier(
                b ->
                    new TranslatableText(
                        b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setDefaultValue(true)
            .setSaveConsumer(b -> config.enableDefaultItemSortingRules = b)
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startStrList(
                new TranslatableText("option.reasonable-sorting.custom_sorting_rules"),
                config.customSortingRules)
            .setTooltip(
                new TranslatableText("option.reasonable-sorting.custom_sorting_rules.tooltip"),
                new TranslatableText("option.reasonable-sorting.custom_sorting_rules.example"))
            .setInsertInFront(false)
            .setExpanded(true)
            .setAddButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_sorting_rules.add"))
            .setRemoveButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_sorting_rules.remove"))
            .setDefaultValue(Collections.emptyList())
            .setCellErrorSupplier(
                s -> {
                  final String[] split = s.split("\\s+");
                  final List<String> invalids =
                      Arrays.stream(split)
                          .filter(((Predicate<String>) String::isEmpty).negate())
                          .filter(s1 -> Identifier.tryParse(s1) == null)
                          .toList();
                  return !invalids.isEmpty()
                      ? Optional.of(
                          new TranslatableText(
                              "option.reasonable-sorting.error.invalid_identifier",
                              String.join(" ", invalids)))
                      : Optional.empty();
                })
            .setSaveConsumer(
                list -> {
                  config.customSortingRules = formatted(list);
                  Configs.updateCustomSortingRules(list, Configs.CUSTOM_SORTING_RULES);
                })
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startTextDescription(
                new TranslatableText(
                    "option.reasonable-sorting.describe_variants",
                    new LiteralText(
                            String.join(
                                " ",
                                Arrays.stream(BlockFamily.Variant.values())
                                    .map(BlockFamily.Variant::getName)
                                    .toList()))
                        .formatted(Formatting.YELLOW)))
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startStrField(
                new TranslatableText("option.reasonable-sorting.variants_following_base_blocks"),
                config.variantsFollowingBaseBlocks)
            .setDefaultValue("stairs slab")
            .setTooltip(
                new TranslatableText(
                    "option.reasonable-sorting.variants_following_base_blocks.tooltip"))
            .setErrorSupplier(
                s -> {
                  List<String> invalidNames = new ArrayList<>();
                  Arrays.stream(s.split("\\s+"))
                      .filter(name -> !name.isEmpty())
                      .filter(name -> !Configs.NAME_TO_VARIANT.containsKey(name))
                      .forEach(invalidNames::add);
                  return invalidNames.isEmpty()
                      ? Optional.empty()
                      : Optional.of(
                          new TranslatableText(
                              "option.reasonable-sorting.error.invalid_variant_name",
                              String.join(", ", invalidNames)));
                })
            .setSaveConsumer(
                s -> {
                  config.variantsFollowingBaseBlocks = s;
                  Configs.updateVariantsFollowingBaseBlocks(s, BlockFamilyRule.AFFECTED_VARIANTS);
                })
            .build());

    if (ExtShapeBridge.modLoaded()) {
      categorySorting.addEntry(
          entryBuilder
              .startTextDescription(
                  new TranslatableText(
                      "option.reasonable-sorting.describe_shapes",
                      new LiteralText(ExtShapeBridge.getValidShapeNames())
                          .formatted(Formatting.YELLOW)))
              .build());
      categorySorting.addEntry(
          entryBuilder
              .startStrField(
                  new TranslatableText("option.reasonable-sorting.shapes_following_base_blocks"),
                  config.shapesFollowingBaseBlocks)
              .setDefaultValue("*")
              .setTooltip(
                  new TranslatableText(
                      "option.reasonable-sorting.shapes_following_base_blocks.tooltip"))
              .setErrorSupplier(
                  s -> {
                    if ("*".equals(s)) {
                      return Optional.empty();
                    }
                    final List<String> invalids =
                        Arrays.stream(s.split("\\s+"))
                            .filter(s1 -> !s1.isEmpty())
                            .filter(s2 -> !ExtShapeBridge.isValidShapeName(s2))
                            .toList();
                    return invalids.isEmpty()
                        ? Optional.empty()
                        : Optional.of(
                            new TranslatableText(
                                "option.reasonable-sorting.error.invalid_shape_name",
                                String.join(" ", invalids)));
                  })
              .setSaveConsumer(
                  s3 -> {
                    config.shapesFollowingBaseBlocks = s3;
                    ExtShapeBridge.updateShapeList(s3);
                  })
              .build());
    }

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.fence_gate_follows_fence"),
                config.fenceGateFollowsFence)
            .setSaveConsumer(b -> config.fenceGateFollowsFence = b)
            .setDefaultValue(true)
            .setTooltip(
                new TranslatableText("option.reasonable-sorting.fence_gate_follows_fence.tooltip"))
            .build());

    // 物品组转移部分。
    categoryTransfer.addEntry(
        entryBuilder
            .startTextDescription(
                new TranslatableText("category.reasonable-sorting.transfer.description"))
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.enable_group_transfer"),
                config.enableGroupTransfer)
            .setDefaultValue(true)
            .setTooltip(
                new TranslatableText("option.reasonable-sorting.enable_group_transfer.tooltip"))
            .setYesNoTextSupplier(
                b ->
                    new TranslatableText(
                        b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setSaveConsumer(b -> config.enableGroupTransfer = b)
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.buttons_in_decorations"),
                config.buttonsInDecorations)
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.buttonsInDecorations = b)
            .build());
    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.fence_gates_in_decorations"),
                config.fenceGatesInDecorations)
            .setDefaultValue(true)
            .setSaveConsumer(b -> config.fenceGatesInDecorations = b)
            .build());
    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.swords_in_tools"),
                config.swordsInTools)
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.swordsInTools = b)
            .build());
    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                new TranslatableText("option.reasonable-sorting.doors_in_decorations"),
                config.doorsInDecorations)
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.doorsInDecorations = b)
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startTextDescription(
                new TranslatableText(
                    "option.reasonable-sorting.describe_item_groups",
                    new LiteralText(
                            String.join(
                                " ",
                                Arrays.stream(ItemGroup.GROUPS).map(ItemGroup::getName).toList()))
                        .formatted(Formatting.YELLOW)))
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules"),
                config.transferRules)
            .setTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(
                s -> {
                  final String[] split = s.split("\\s+");
                  if (split.length > 2) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.unexpected_text", split[2]));
                  }
                  if (split.length < 2) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.group_name_expected"));
                  }
                  if (Identifier.tryParse(split[0]) == null) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.invalid_identifier", split[0]));
                  }
                  return Optional.empty();
                })
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.transferRules = list;
                  Configs.updateCustomTransferRule(list, Configs.CUSTOM_TRANSFER_RULE);
                })
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                new TranslatableText("option.reasonable-sorting.custom_variant_transfer_rules"),
                config.variantTransferRules)
            .setTooltip(
                new TranslatableText(
                    "option.reasonable-sorting.custom_variant_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(
                s -> {
                  if (s.isEmpty()) {
                    return Optional.empty();
                  }
                  final String[] split = s.split("\\s+");
                  if (split.length > 2) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.unexpected_text", split[2]));
                  }
                  if (split.length < 2) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.group_name_expected"));
                  }
                  if (!Configs.NAME_TO_VARIANT.containsKey(split[0])) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.invalid_variant_name", split[0]));
                  }
                  return Optional.empty();
                })
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.variantTransferRules = list;
                  Configs.updateCustomVariantTransferRules(
                      list, Configs.CUSTOM_VARIANT_TRANSFER_RULE);
                })
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                new TranslatableText("option.reasonable-sorting.custom_regex_transfer_rules"),
                config.regexTransferRules)
            .setTooltip(
                new TranslatableText(
                    "option.reasonable-sorting.custom_regex_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                new TranslatableText("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(
                s -> {
                  if (s.isEmpty()) {
                    return Optional.empty();
                  }
                  final String[] split = s.split("\\s+");
                  if (split.length > 2) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.unexpected_text", split[2]));
                  }
                  if (split.length < 2) {
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.group_name_expected"));
                  }
                  final String pattern = split[0];
                  try {
                    Pattern.compile(pattern);
                  } catch (PatternSyntaxException e) {
                    final int index = e.getIndex();
                    return Optional.of(
                        new TranslatableText(
                            "option.reasonable-sorting.error.invalid_regex",
                            e.getDescription(),
                            new LiteralText("")
                                .append(pattern.substring(0, index))
                                .append("»")
                                .append(
                                    new LiteralText(
                                            index < pattern.length()
                                                ? pattern.substring(index, index + 1)
                                                : "")
                                        .formatted(Formatting.DARK_RED))
                                .append("«")
                                .append(
                                    new LiteralText(
                                        index + 1 < pattern.length()
                                            ? pattern.substring(index + 1)
                                            : ""))));
                  }
                  return Optional.empty();
                })
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.regexTransferRules = list;
                  Configs.updateCustomRegexTransferRules(list, Configs.CUSTOM_REGEX_TRANSFER_RULE);
                })
            .build());

    if (ExtShapeBridge.modLoaded()) {
      categoryTransfer
          .addEntry(
              entryBuilder
                  .startStrList(
                      new TranslatableText("option.reasonable-sorting.custom_shape_transfer_rules"),
                      config.shapeTransferRules)
                  .setDefaultValue(Collections.emptyList())
                  .setTooltip(
                      new TranslatableText(
                          "option.reasonable-sorting.custom_shape_transfer_rules.tooltip"))
                  .setExpanded(true)
                  .setInsertInFront(false)
                  .setAddButtonTooltip(
                      new TranslatableText("option.reasonable-sorting.custom_transfer_rules.add"))
                  .setRemoveButtonTooltip(
                      new TranslatableText(
                          "option.reasonable-sorting.custom_transfer_rules.remove"))
                  .setCellErrorSupplier(
                      s -> {
                        if (s.isEmpty()) {
                          return Optional.empty();
                        }
                        final String[] split = s.split("\\s+");
                        if (split.length > 2) {
                          return Optional.of(
                              new TranslatableText(
                                  "option.reasonable-sorting.error.unexpected_text", split[2]));
                        }
                        if (split.length < 2) {
                          return Optional.of(
                              new TranslatableText(
                                  "option.reasonable-sorting.error.group_name_expected"));
                        }
                        if (!ExtShapeBridge.isValidShapeName(split[0])) {
                          return Optional.of(
                              new TranslatableText(
                                  "option.reasonable-sorting.error.invalid_shape_name", split[0]));
                        }
                        return Optional.empty();
                      })
                  .setSaveConsumer(
                      list -> {
                        config.shapeTransferRules = list;
                        ExtShapeBridge.updateShapeTransferRules(list);
                      })
                  .build())
          .addEntry(
              entryBuilder
                  .startBooleanToggle(
                      new TranslatableText(
                          "option.reasonable-sorting.base_blocks_in_building_blocks"),
                      config.baseBlocksInBuildingBlocks)
                  .setDefaultValue(true)
                  .setTooltip(
                      new TranslatableText(
                          "option.reasonable-sorting.base_blocks_in_building_blocks.tooltip"))
                  .setSaveConsumer(
                      b -> {
                        config.baseBlocksInBuildingBlocks = b;
                        ExtShapeBridge.setBaseBlocksInBuildingBlocks(b);
                      })
                  .build());
    }

    return builder.build();
  }
}
