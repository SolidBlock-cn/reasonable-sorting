package pers.solid.mod;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SolidBlock
 */
@Environment(EnvType.CLIENT)
public class ConfigScreen {
  public static List<String> formatted(List<String> list) {
    List<String> newList = new ArrayList<>();
    for (String s : list) {
      newList.add(Arrays.stream(s.split("\\s+"))
          .map(Identifier::tryParse)
          .filter(Objects::nonNull)
          .map(Identifier::toString)
          .collect(Collectors.joining(" ")));
    }
    return newList;
  }

  /**
   * 模组配置屏幕创建。
   *
   * @param previousScreen 上层屏幕。
   * @return 配置屏幕。
   */
  public Screen createScreen(Screen previousScreen) {
    final Configs config = Configs.CONFIG_HOLDER.getConfig();
    ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(previousScreen)
        .setSavingRunnable(Configs.CONFIG_HOLDER::save)
        .setTitle(Text.translatable("title.reasonable-sorting.config"));

    ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    ConfigCategory categorySorting =
        builder.getOrCreateCategory(Text.translatable("category.reasonable-sorting.sorting"));
    categorySorting.setDescription(
        new Text[]{
            Text.translatable("category.reasonable-sorting.sorting.description")
        });
    ConfigCategory categoryTransfer =
        builder.getOrCreateCategory(Text.translatable("category.reasonable-sorting.transfer"));
    categoryTransfer.setDescription(
        new Text[]{
            Text.translatable("category.reasonable-sorting.transfer.description")
        });

    // 排序部分。
    categorySorting.addEntry(
        entryBuilder
            .startTextDescription(
                Text.translatable("category.reasonable-sorting.sorting.description"))
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.enable_sorting"),
                config.enableSorting)
            .setTooltip(Text.translatable("option.reasonable-sorting.enable_sorting.tooltip"))
            .setYesNoTextSupplier(
                b -> Text.translatable(b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setDefaultValue(true)
            .setSaveConsumer(b -> config.enableSorting = b)
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startEnumSelector(
                Text.translatable("option.reasonable-sorting.sorting_influence_range"),
                SortingInfluenceRange.class,
                config.sortingInfluenceRange)
            .setTooltip(Text.translatable("option.reasonable-sorting.sorting_influence_range.tooltip").append("\n").append(
                Texts.join(Arrays.stream(SortingInfluenceRange.values())
                    .map(v -> Text.literal(" - ").append(v.getName().formatted(Formatting.YELLOW)).append(" - ").append(v.getDescription().formatted(Formatting.GRAY)))
                    .toList(), Text.literal("\n"))
            ))
            .setEnumNameProvider(anEnum -> ((SortingInfluenceRange) anEnum).getName())
            .setDefaultValue(SortingInfluenceRange.INVENTORY_ONLY)
            .setSaveConsumer(b -> config.sortingInfluenceRange = b)
            .build());
    categorySorting.addEntry(
        entryBuilder
            .startEnumSelector(
                Text.translatable("option.reasonable-sorting.sorting_calculation_type"),
                SortingCalculationType.class,
                config.sortingCalculationType)
            .setTooltip(Text.translatable("option.reasonable-sorting.sorting_calculation_type.tooltip").append("\n").append(
                Texts.join(Arrays.stream(SortingCalculationType.values())
                    .map(v -> Text.literal(" - ").append(v.getName().formatted(Formatting.YELLOW)).append(" - ").append(v.getDescription().formatted(Formatting.GRAY)))
                    .toList(), Text.literal("\n"))
            ))
            .setEnumNameProvider(anEnum -> ((SortingCalculationType) anEnum).getName())
            .setDefaultValue(SortingCalculationType.STANDARD)
            .setSaveConsumer(b -> config.sortingCalculationType = b)
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.debug_mode"),
                config.debugMode)
            .setTooltip(Text.translatable("option.reasonable-sorting.debug_mode.tooltip"))
            .setYesNoTextSupplier(
                b -> Text.translatable(b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.debugMode = b)
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.enable_default_item_sorting_rules"),
                config.enableDefaultItemSortingRules)
            .setTooltip(
                Text.translatable(
                    "option.reasonable-sorting.enable_default_item_sorting_rules.tooltip"))
            .setYesNoTextSupplier(
                b -> Text.translatable(b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setDefaultValue(true)
            .setSaveConsumer(b -> config.enableDefaultItemSortingRules = b)
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startStrList(
                Text.translatable("option.reasonable-sorting.custom_sorting_rules"),
                config.customSortingRules)
            .setTooltip(
                Text.translatable("option.reasonable-sorting.custom_sorting_rules.tooltip"),
                Text.translatable("option.reasonable-sorting.custom_sorting_rules.example"))
            .setInsertInFront(false)
            .setExpanded(true)
            .setAddButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_sorting_rules.add"))
            .setRemoveButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_sorting_rules.remove"))
            .setDefaultValue(Collections.emptyList())
            .setCellErrorSupplier(ConfigsHelper::validateCustomSortingRule)
            .setSaveConsumer(
                list -> {
                  config.customSortingRules = formatted(list);
                  ConfigsHelper.updateCustomSortingRules(list, Configs.CUSTOM_ITEM_SORTING_RULES);
                })
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startTextDescription(Text.translatable(
                "option.reasonable-sorting.describe_variants",
                Text.literal(
                        Arrays.stream(BlockFamily.Variant.values())
                            .map(BlockFamily.Variant::getName)
                            .collect(Collectors.joining(StringUtils.SPACE)))
                    .formatted(Formatting.YELLOW)))
            .build());

    categorySorting.addEntry(
        entryBuilder
            .startStrField(
                Text.translatable("option.reasonable-sorting.variants_following_base_blocks"),
                config.variantsFollowingBaseBlocks)
            .setDefaultValue("stairs slab")
            .setTooltip(
                Text.translatable("option.reasonable-sorting.variants_following_base_blocks.tooltip"))
            .setErrorSupplier(ConfigsHelper::validateVariantFollowsBaseBlocks)
            .setSaveConsumer(
                s -> {
                  config.variantsFollowingBaseBlocks = s;
                  ConfigsHelper.updateVariantsFollowingBaseBlocks(s, Configs.VARIANTS_FOLLOWING_BASE_BLOCKS);
                })
            .build());

    if (ExtShapeBridge.INSTANCE.modHasLoaded()) {
      categorySorting.addEntry(
          entryBuilder
              .startTextDescription(
                  Text.translatable(
                      "option.reasonable-sorting.describe_shapes",
                      Text.literal(ExtShapeBridge.INSTANCE.getValidShapeNames().collect(Collectors.joining(" ")))
                          .formatted(Formatting.YELLOW)))
              .build());
      categorySorting.addEntry(
          entryBuilder
              .startStrField(
                  Text.translatable("option.reasonable-sorting.shapes_following_base_blocks"),
                  config.shapesFollowingBaseBlocks)
              .setDefaultValue("*")
              .setTooltip(
                  Text.translatable(
                      "option.reasonable-sorting.shapes_following_base_blocks.tooltip"))
              .setErrorSupplier(ConfigsHelper::validateShapeFollowsBaseBlocks)
              .setSaveConsumer(
                  s3 -> {
                    config.shapesFollowingBaseBlocks = s3;
                    ExtShapeBridge.INSTANCE.updateShapeList(s3);
                  })
              .build());
    }

    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.fence_gate_follows_fence"),
                config.fenceGateFollowsFence)
            .setSaveConsumer(b -> config.fenceGateFollowsFence = b)
            .setDefaultValue(true)
            .setTooltip(
                Text.translatable("option.reasonable-sorting.fence_gate_follows_fence.tooltip"))
            .build());
    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.fancy_color_sorting"),
                config.fancyColorsSorting)
            .setSaveConsumer(b -> config.fancyColorsSorting = b)
            .setDefaultValue(true)
            .setTooltip(
                Text.translatable("option.reasonable-sorting.fancy_color_sorting.tooltip"))
            .build());
    categorySorting.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.block_items_only"),
                config.blockItemsOnly)
            .setSaveConsumer(b -> config.blockItemsOnly = b)
            .setDefaultValue(false)
            .setTooltip(
                Text.translatable("option.reasonable-sorting.block_items_only.tooltip"))
            .build());

    // 物品组转移部分。
    categoryTransfer.addEntry(
        entryBuilder
            .startTextDescription(
                Text.translatable("category.reasonable-sorting.transfer.description"))
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.enable_group_transfer"),
                config.enableGroupTransfer)
            .setDefaultValue(true)
            .setTooltip(
                Text.translatable("option.reasonable-sorting.enable_group_transfer.tooltip"))
            .setYesNoTextSupplier(
                b -> Text.translatable(b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
            .setSaveConsumer(b -> config.enableGroupTransfer = b)
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.buttons_in_decorations"),
                config.buttonsInDecorations)
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.buttonsInDecorations = b)
            .build());
    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.fence_gates_in_decorations"),
                config.fenceGatesInDecorations)
            .setDefaultValue(true)
            .setSaveConsumer(b -> config.fenceGatesInDecorations = b)
            .build());
    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.swords_in_tools"),
                config.swordsInTools)
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.swordsInTools = b)
            .build());
    categoryTransfer.addEntry(
        entryBuilder
            .startBooleanToggle(
                Text.translatable("option.reasonable-sorting.doors_in_decorations"),
                config.doorsInDecorations)
            .setDefaultValue(false)
            .setSaveConsumer(b -> config.doorsInDecorations = b)
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startTextDescription(
                Text.translatable(
                    "option.reasonable-sorting.describe_item_groups",
                    Text.literal(Arrays.stream(ItemGroup.GROUPS).map(ItemGroup::getName).collect(Collectors.joining(" ")))
                        .formatted(Formatting.YELLOW)))
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules"),
                config.transferRules)
            .setTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(ConfigsHelper::validateCustomTransferRule)
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.transferRules = list;
                  ConfigsHelper.updateCustomTransferRule(list, Configs.CUSTOM_TRANSFER_RULE);
                })
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                Text.translatable("option.reasonable-sorting.custom_variant_transfer_rules"),
                config.variantTransferRules)
            .setTooltip(
                Text.translatable(
                    "option.reasonable-sorting.custom_variant_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(ConfigsHelper::validateCustomVariantTransferRule)
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.variantTransferRules = list;
                  ConfigsHelper.updateCustomVariantTransferRules(
                      list, Configs.CUSTOM_VARIANT_TRANSFER_RULE);
                })
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                Text.translatable("option.reasonable-sorting.custom_regex_transfer_rules"),
                config.regexTransferRules)
            .setTooltip(
                Text.translatable(
                    "option.reasonable-sorting.custom_regex_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(ConfigsHelper::validateCustomRegexTransferRule)
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.regexTransferRules = list;
                  ConfigsHelper.updateCustomRegexTransferRules(list, Configs.CUSTOM_REGEX_TRANSFER_RULE);
                })
            .build());

    categoryTransfer.addEntry(
        entryBuilder
            .startStrList(
                Text.translatable("option.reasonable-sorting.custom_tag_transfer_rules"),
                config.tagTransferRules)
            .setTooltip(
                Text.translatable(
                    "option.reasonable-sorting.custom_tag_transfer_rules.tooltip"))
            .setExpanded(true)
            .setInsertInFront(false)
            .setAddButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.add"))
            .setRemoveButtonTooltip(
                Text.translatable("option.reasonable-sorting.custom_transfer_rules.remove"))
            .setCellErrorSupplier(ConfigsHelper::validateCustomTagTransferRule)
            .setDefaultValue(Collections.emptyList())
            .setSaveConsumer(
                list -> {
                  config.tagTransferRules = list;
                  ConfigsHelper.updateCustomTagTransferRules(list, Configs.CUSTOM_TAG_TRANSFER_RULE);
                })
            .build());

    if (ExtShapeBridge.INSTANCE.modHasLoaded()) {
      categoryTransfer
          .addEntry(
              entryBuilder
                  .startStrList(
                      Text.translatable("option.reasonable-sorting.custom_shape_transfer_rules"),
                      config.shapeTransferRules)
                  .setDefaultValue(Collections.emptyList())
                  .setTooltip(
                      Text.translatable(
                          "option.reasonable-sorting.custom_shape_transfer_rules.tooltip"))
                  .setExpanded(true)
                  .setInsertInFront(false)
                  .setAddButtonTooltip(
                      Text.translatable("option.reasonable-sorting.custom_transfer_rules.add"))
                  .setRemoveButtonTooltip(
                      Text.translatable(
                          "option.reasonable-sorting.custom_transfer_rules.remove"))
                  .setCellErrorSupplier(ConfigsHelper::validateCustomShapeTransferRule)
                  .setSaveConsumer(
                      list -> {
                        config.shapeTransferRules = list;
                        ExtShapeBridge.INSTANCE.updateShapeTransferRules(list);
                      })
                  .build())
          .addEntry(
              entryBuilder
                  .startBooleanToggle(
                      Text.translatable(
                          "option.reasonable-sorting.base_blocks_in_building_blocks"),
                      config.baseBlocksInBuildingBlocks)
                  .setDefaultValue(true)
                  .setTooltip(
                      Text.translatable(
                          "option.reasonable-sorting.base_blocks_in_building_blocks.tooltip"))
                  .setSaveConsumer(
                      b -> config.baseBlocksInBuildingBlocks = b)
                  .build());
    }

    return builder.build();
  }
}
