package pers.solid.mod;

import org.apache.commons.lang3.StringUtils;
import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.ConfigEnvironment;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.loader.impl.QuiltLoaderImpl;

public class QuiltConfigs extends Configs {
  public static final Config QUILT_CONFIG = Config.create(new ConfigEnvironment(QuiltLoaderImpl.INSTANCE.getConfigDir(), new JsonSerializerQuilt()), "", "reasonable-sorting", builder -> builder
      .format("json")
      .field(TrackedValue.create(true, "enableSorting", builder1 -> builder1.callback(v -> Configs.instance.enableSorting = v.value())))
      .field(TrackedValue.create(true, "enableDefaultItemSortingRules", builder1 -> builder1.callback(v -> Configs.instance.enableDefaultItemSortingRules = v.value())))
      .field(TrackedValue.create(ValueList.create(StringUtils.EMPTY), "customSortingRules", builder1 -> builder1.callback(v -> {
        Configs.instance.customSortingRules = v.value();
        ConfigsHelper.updateCustomSortingRules(v.value(), Configs.CUSTOM_ITEM_SORTING_RULES);
      })))
      .field(TrackedValue.create("stairs slab", "variantsFollowingBaseBlocks", builder1 -> builder1.callback(v -> {
        Configs.instance.variantsFollowingBaseBlocks = v.value();
        ConfigsHelper.updateVariantsFollowingBaseBlocks(v.value(), Configs.VARIANTS_FOLLOWING_BASE_BLOCKS);
      })))
      .field(TrackedValue.create(true, "fenceGateFollowsFence", builder1 -> builder1.callback(v -> Configs.instance.fenceGateFollowsFence = v.value())))
      .field(TrackedValue.create(false, "blockItemsOnly", builder1 -> builder1.callback(v -> Configs.instance.blockItemsOnly = v.value())))
      .field(TrackedValue.create(true, "enableGroupTransfer", builder1 -> builder1.callback(v -> Configs.instance.enableGroupTransfer = v.value())))
      .field(TrackedValue.create(false, "buttonsInDecorations", builder1 -> builder1.callback(v -> Configs.instance.buttonsInDecorations = v.value())))
      .field(TrackedValue.create(true, "fenceGatesInDecorations", builder1 -> builder1.callback(v -> Configs.instance.fenceGatesInDecorations = v.value())))
      .field(TrackedValue.create(false, "swordsInTools", builder1 -> builder1.callback(v -> Configs.instance.swordsInTools = v.value())))
      .field(TrackedValue.create(false, "doorsInDecorations", builder1 -> builder1.callback(v -> Configs.instance.doorsInDecorations = v.value())))
      .field(TrackedValue.create(ValueList.create(""), "transferRules", builder1 -> builder1.callback(v -> {
        Configs.instance.transferRules = v.value();
        ConfigsHelper.updateCustomTransferRule(v.value(), Configs.CUSTOM_TRANSFER_RULE);
      })))
      .field(TrackedValue.create(ValueList.create(""), "variantTransferRules", builder1 -> builder1.callback(v -> {
        Configs.instance.variantTransferRules = v.value();
        ConfigsHelper.updateCustomVariantTransferRules(v.value(), Configs.CUSTOM_VARIANT_TRANSFER_RULE);
      })))
      .field(TrackedValue.create(ValueList.create(""), "regexTransferRules", builder1 -> builder1.callback(v -> {
        Configs.instance.regexTransferRules = v.value();
        ConfigsHelper.updateCustomRegexTransferRules(v.value(), Configs.CUSTOM_REGEX_TRANSFER_RULE);
      })))
  );

  static {
    QUILT_CONFIG.registerCallback(config -> {
      ConfigsHelper.updateCustomSortingRules(Configs.instance.customSortingRules, Configs.CUSTOM_ITEM_SORTING_RULES);
      ConfigsHelper.updateCustomTransferRule(Configs.instance.transferRules, Configs.CUSTOM_TRANSFER_RULE);
      ConfigsHelper.updateCustomRegexTransferRules(Configs.instance.regexTransferRules, Configs.CUSTOM_REGEX_TRANSFER_RULE);
      ConfigsHelper.updateCustomVariantTransferRules(Configs.instance.variantTransferRules, Configs.CUSTOM_VARIANT_TRANSFER_RULE);
      ConfigsHelper.updateVariantsFollowingBaseBlocks(Configs.instance.variantsFollowingBaseBlocks, Configs.VARIANTS_FOLLOWING_BASE_BLOCKS);
    });
  }
}
