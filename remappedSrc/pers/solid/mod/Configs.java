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
     * 是否启用默认的物品排序规则。
     *
     * @see DefaultItemSortingRuleSupplier
     */
    public boolean enableDefaultItemSortingRules = true;
    /**
     * 自定义排序规则。每行一条规则，由多个物品的命名空间 id 组成，用空格隔开。<br>
     * 例如：{@code grass_block dirt dirt_path} 表示将草方块、泥土、土径排在一起，以草方块的位置为准。
     *
     * @see ConfigScreen#CUSTOM_SORTING_RULES
     * @see ConfigScreen#updateCustomSortingRules
     */
    public List<String> customSortingRules = new ArrayList<>();
    /**
     * 受排序规则影响的方块变种。这些变种中的方块物品都会排在其基础方块之后。每次保存、修改配置时，都会对 {@link BlockFamilyRule#AFFECTED_VARIANTS AFFECTED_VARIANTS} 进行重写。
     *
     * @see BlockFamilyRule#AFFECTED_VARIANTS
     * @see ConfigScreen#updateVariantsFollowingBaseBlocks
     */
    public String variantsFollowingBaseBlocks = "stairs slab";
    /**
     * 受排序规则影响的 ExtShape 模组中的方块变种。
     */
    public String shapesFollowingBaseBlocks = "*";
    /**
     * 栅栏门紧随栅栏。需要注意的是，该项需要和 {@link #fenceGatesInDecorations} 搭配使用。
     *
     * @see BlockFamilyRule#FENCE_TO_FENCE_GATES
     */
    public boolean fenceGateFollowsFence = true;
    /**
     * <h2>物品组转移部分</h2>
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
     * @see ConfigScreen#CUSTOM_TRANSFER_RULE
     * @see ConfigScreen#updateCustomTransferRule
     */
    public List<String> transferRules = new ArrayList<>();
    /**
     * 自定义物品变种转移规则。
     *
     * @see ConfigScreen#CUSTOM_VARIANT_TRANSFER_RULE
     * @see ConfigScreen#updateCustomVariantTransferRules
     */
    public List<String> variantTransferRules = new ArrayList<>();
    /**
     * 自定义正则表达式转移规则。
     *
     * @see ConfigScreen#CUSTOM_REGEX_TRANSFER_RULE
     * @see ConfigScreen#updateCustomRegexTransferRules
     */
    public List<String> regexTransferRules = new ArrayList<>();
}
