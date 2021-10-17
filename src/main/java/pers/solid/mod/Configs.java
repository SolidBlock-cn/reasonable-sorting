package pers.solid.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.ArrayList;
import java.util.List;

@me.shedaniel.autoconfig.annotation.Config(name = "reasonable-sorting")
public class Configs implements ConfigData {
    @ConfigEntry.Category("sorting_rules")
    List<String> customSortingRulesStrList = new ArrayList<>();

    @ConfigEntry.Category("transfer_rules")
    boolean buttonsInDecorations = false;
    boolean fenceGatesInDecorations = false;
    boolean swordsInTools = false;
    List<String> transferRules = new ArrayList<>();
    List<String> regexTransferRules = new ArrayList<>();
    public static final ConfigHolder<Configs> CONFIG_HOLDER = AutoConfig.register(Configs.class, GsonConfigSerializer::new);
    public static final Configs CONFIGS = CONFIG_HOLDER.getConfig();
}
