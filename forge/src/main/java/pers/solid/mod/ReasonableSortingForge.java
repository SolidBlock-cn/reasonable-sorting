package pers.solid.mod;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Mod("reasonable-sorting")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReasonableSortingForge {
  public static final ForgeConfigSpec CONFIG = new ForgeConfigSpec.Builder()
      .define("reasonable-sorting.enable_sorting", false)
      .next()
      .define("reasonable-sorting.enable_default_item_sorting_rules", false)
      .next()
      .define("reasonable-sorting.custom_sorting_rules", new ArrayList<String>(), o -> {
        if (o instanceof Iterable iterable) {
          for (Object o1 : iterable) {
            if (o1 instanceof String s1 && ConfigsHelper.validateCustomSortingRule(s1).isEmpty()) continue;
            return false;
          }
          return true;
        }
        return false;
      })
      .next()
      .build();
  public static final Logger LOGGER = LoggerFactory.getLogger(ReasonableSortingForge.class);

  static {
    Configs.instance = new Configs();
  }

  public ReasonableSortingForge() {
    SortingRules.initialize();
    TransferRules.initialize();
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
  }

  @SubscribeEvent
  public static void loadConfig(ModConfigEvent.Loading event) {
    final ModConfig config = event.getConfig();
    final CommentedConfig configData = config.getConfigData();
    if (config.getSpec() != CONFIG) return;
    Configs.instance.enableSorting = configData.get("reasonable-sorting.enable_sorting");
    Configs.instance.enableDefaultItemSortingRules = configData.get("reasonable-sorting.enable_default_item_sorting_rules");
    Configs.instance.customSortingRules = configData.get("reasonable-sorting.custom_sorting_rules");
    ConfigsHelper.updateCustomSortingRules(Configs.instance.customSortingRules, Configs.CUSTOM_ITEM_SORTING_RULES);
  }
}
