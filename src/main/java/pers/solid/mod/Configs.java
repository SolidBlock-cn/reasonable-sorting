package pers.solid.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 此模组的配置储存。本模组使用 Auto Config 进行配置文件的读取与保存，但是配置界面仍是通过 Cloth Config 进行手动创建的。
 *
 * @see ConfigScreen
 */
@Config(name = "reasonable-sorting")
public class Configs implements ConfigData {
    public static final ConfigHolder<Configs> CONFIG_HOLDER = AutoConfig.register(Configs.class, GsonConfigSerializer::new);
    /**
     * <h2>排序部分</h2>
     * 是否启用排序。如果该项为 {@code false}，则所有的排序都会按照原版进行。
     */
    public boolean enableSorting = true;
    /**
     * 是否启用默认的物品组合排序规则。
     *
     * @see DefaultItemCombinationRuleSupplier
     */
    public boolean enableDefaultItemCombinationRules = true;
    /**
     * 自定义排序规则。每行一条规则，由多个物品的命名空间 id 组成，用空格隔开。<br>
     * 例如：{@code grass_block dirt dirt_path} 表示将草方块、泥土、土径排在一起，以草方块的位置为准。
     */
    public List<String> customSortingRules = new ArrayList<>();
    /**
     * 受排序规则影响的方块变种。这些变种中的方块物品都会排在其基础方块之后。每次保存、修改配置时，都会对 {@link BlockFamilyRule#AFFECTED_VARIANTS AFFECTED_VARIANTS} 进行重写。
     *
     * @see BlockFamilyRule#AFFECTED_VARIANTS
     */
    public String variantsFollowingBaseBlocks = "stairs slab";
    public boolean fenceGateFollowsFence = true;
    /**
     * <h2>物品组转移部分</h2>
     * 是否启用物品组转移。如果该项为 <code>false</code>，则所有的物品组转移都会按照原版进行。
     */
    public boolean enableGroupTransfer = true;
    public boolean buttonsInDecorations = false;
    public boolean fenceGatesInDecorations = true;
    public boolean swordsInTools = false;
    public boolean doorsInDecorations = false;
    public List<String> transferRules = new ArrayList<>();
    public List<String> variantTransferRules = new ArrayList<>();
    public List<String> regexTransferRules = new ArrayList<>();
}
