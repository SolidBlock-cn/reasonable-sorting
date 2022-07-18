package pers.solid.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.util.ActionResult;

/**
 * 本模组的 Fabric 版本配置。本模组使用 Auto Config 进行配置文件的读取与保存，但是配置界面仍是通过 Cloth Config 进行手动创建的。
 */
@Config(name = "reasonable-sorting")
public class FabricConfigs extends Configs implements ConfigData {
  public static final ConfigHolder<FabricConfigs> CONFIG_HOLDER = AutoConfig.register(FabricConfigs.class, GsonConfigSerializer::new);

  static {
    CONFIG_HOLDER.registerLoadListener((configHolder, fabricConfigs) -> {
      Configs.instance = fabricConfigs;
      ConfigsHelper.updateCustomSortingRules(Configs.instance.customSortingRules, Configs.CUSTOM_ITEM_SORTING_RULES);
      ConfigsHelper.updateCustomTransferRule(Configs.instance.transferRules, Configs.CUSTOM_TRANSFER_RULE);
      ConfigsHelper.updateCustomRegexTransferRules(Configs.instance.regexTransferRules, Configs.CUSTOM_REGEX_TRANSFER_RULE);
      ConfigsHelper.updateCustomVariantTransferRules(Configs.instance.variantTransferRules, Configs.CUSTOM_VARIANT_TRANSFER_RULE);
      ConfigsHelper.updateVariantsFollowingBaseBlocks(Configs.instance.variantsFollowingBaseBlocks, Configs.VARIANTS_FOLLOWING_BASE_BLOCKS);
      return ActionResult.SUCCESS;
    });
    CONFIG_HOLDER.registerSaveListener((configHolder, fabricConfigs) -> {
      Configs.instance = fabricConfigs;
      return ActionResult.SUCCESS;
    });
  }
}
