package pers.solid.mod;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.Immutable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MixinHelper implements ModInitializer {
    /**
     * 存放所有物品组合规则的列表。
     */
    public static final Collection<Map<Item, ? extends Collection<Item>>> ITEM_COMBINATION_RULES = new ObjectArrayList<>();
    /**
     * 存放所有物品组转移规则的列表。
     */
    public static final ObjectList<Map<Item, ItemGroup>> ITEM_GROUP_TRANSFER_RULES = new ObjectArrayList<>();
    /**
     * 类似于 {@link #COMPILED_ITEM_GROUP_TRANSFER_RULES}，不过它是实时的、抽象的，在修改配置之后不需要重新编译。
     */
    @Immutable
    public static final Map<Item, ImmutableCollection<ItemGroup>> ABSTRACT_ITEM_GROUP_TRANSFER_RULES = new AbstractMap<>() {
        @NotNull
        @Override
        public Set<Entry<Item, ImmutableCollection<ItemGroup>>> entrySet() {
            ImmutableSet.Builder<Entry<Item, ImmutableCollection<ItemGroup>>> builder = new ImmutableSet.Builder<>();
            for (Item item : Registry.ITEM) {
                final @Nullable ImmutableCollection<ItemGroup> itemGroups = this.get(item);
                if (itemGroups != null) builder.add(new SimpleImmutableEntry<>(item, itemGroups));
            }
            return builder.build();
        }

        @Override
        @Nullable
        public ImmutableCollection<ItemGroup> get(Object key) {
            ImmutableCollection.Builder<ItemGroup> builder = new ImmutableList.Builder<>();
            for (Map<Item, ItemGroup> itemGroupTransferRule : ITEM_GROUP_TRANSFER_RULES) {
                final @Nullable ItemGroup itemGroup = itemGroupTransferRule.get(key);
                if (itemGroup != null) builder.add(itemGroup);
            }
            final ImmutableCollection<ItemGroup> build = builder.build();
            return build.isEmpty() ? null : build;
        }
    };
    /**
     * 编译后的物品组转移规则。由单个物品映射到物品组集合（考虑到不同的设定可能导致同一个物品转移到多个物品组）。编译是为了加快读取速度，每一次修改配置都会重新编译一次。编译时会清空、重写此列表内容，不会更改此列表指针。
     */
    @Deprecated
    public static final Map<Item, ImmutableCollection<ItemGroup>> COMPILED_ITEM_GROUP_TRANSFER_RULES = new HashMap<>();

    public static final Logger LOGGER = LogManager.getLogger("REASONABLE_SORTING");

    /**
     * 对一个物品应用行为的同时，对其跟随物品也应用行为，如果有。
     */
    public static <T> void applyItemWithFollowings(Collection<? extends Map<T, ? extends Collection<T>>> rules, T item, Consumer<T> action, Consumer<T> actionForFollowings) {
        action.accept(item);
        Collection<T> followings = new LinkedHashSet<>(); // 元素不重复
        for (Map<T, ? extends Collection<T>> rule : rules) {
            if (rule.containsKey(item)) {
                followings.addAll(rule.get(item));
            }
        }
        followings.forEach(actionForFollowings);
    }

    /**
     * 对一个物品及其跟随者应用某一个行为。此方法以泛化，可以不仅针对物品。
     *
     * @param rules  物品排序规则。
     * @param item   物品。（当然也可以是其他类的。）
     * @param action 需要应用的行为。
     * @param <T>    参数的类，比如 {@link Item}。
     */
    public static <T> void applyItemWithFollowings(Collection<? extends Map<T, ? extends Collection<T>>> rules, T item, Consumer<T> action) {
        applyItemWithFollowings(rules, item, action,
                followingItem -> {
                    applyItemWithFollowings(rules, followingItem, action);
                }
        );
    }

    /**
     * 用于 {@link pers.solid.mod.mixin.SimpleRegistryMixin}。
     */
    public static <T> Iterator<T> itemRegistryIterator(ObjectList<T> rawIdToEntry, Collection<? extends Map<T, ? extends Collection<T>>> rules) {
        Set<T> list = new LinkedHashSet<>();
        for (T item : rawIdToEntry) {
            if (isCombinationFollower(item, rules)) continue;
            applyItemWithFollowings(rules, item, list::add);
        }
        if (rawIdToEntry.size() != list.size()) {
            LOGGER.error("Error found when trying to iterate! The size of raw list (%s) does not equal to that of the refreshed list (%s)!".formatted(rawIdToEntry.size(), list.size()));
            for (T item : rawIdToEntry) {
                if (!list.contains(item)) {
                    LOGGER.error("Item %s is not in the refreshed list!".formatted(item));
                }
            }
        }
        return list.iterator();
    }

    /**
     * 判断某个物品是否为物品组合的物品跟随。主要用于判断迭代物品时是否需要跳过该物品。
     *
     * @see #applyItemWithFollowings
     */
    public static <T> boolean isCombinationFollower(T item, Collection<? extends Map<T, ? extends Collection<T>>> rules) {
        for (Map<T, ? extends Collection<T>> rule : rules) {
            for (Collection<T> value : rule.values()) {
                if (value.contains(item)) return true;
            }
        }
        return false;
    }

    /**
     * 将所有的物品组转移规则编译到 {@link #COMPILED_ITEM_GROUP_TRANSFER_RULES} 中以供使用。之所以编译是为了加快读取速度。在修改配置时，也会对物品组进行重新编译。<br>
     * 但是，考虑到第三方模组也会修改配置，而其 {@code saveConsumer} 不会触发重新编译，所以这里也暂时先不重新编译。
     *
     * @param itemGroupTransferRules 物品组转换规则，是由物品到物品组的映射的列表。
     */
    @Deprecated
    public static void compileItemGroupTransferRules(ObjectList<Map<Item, ItemGroup>> itemGroupTransferRules) {
        COMPILED_ITEM_GROUP_TRANSFER_RULES.clear();
        for (Item item : Registry.ITEM) {
            ImmutableSet.Builder<ItemGroup> builder = new ImmutableSet.Builder<>();
            for (Map<Item, ItemGroup> itemGroupTransferRule : itemGroupTransferRules) {
                final @Nullable ItemGroup itemGroup = itemGroupTransferRule.get(item);
                if (itemGroup != null) builder.add(itemGroup);
            }
            final ImmutableSet<ItemGroup> build = builder.build();
            if (!build.isEmpty())
                COMPILED_ITEM_GROUP_TRANSFER_RULES.put(item, build);
        }
    }

    public void onInitialize() {
        // 从配置文件中加载配置。如果文件不存在，则会使用默认的。
        Configs.CONFIG_HOLDER.load();
        // 加载配置文件之后，还需要从配置文件导入信息。
        final Configs configs = Configs.CONFIG_HOLDER.get();
        ConfigScreen.updateVariantsFollowingBaseBlocks(configs.variantsFollowingBaseBlocks, BlockFamilyRule.AFFECTED_VARIANTS);
        ConfigScreen.updateCustomSortingRules(configs.customSortingRules, ConfigScreen.CUSTOM_SORTING_RULES);
        ConfigScreen.updateCustomTransferRule(configs.transferRules, ConfigScreen.CUSTOM_TRANSFER_RULE);
        ConfigScreen.updateCustomVariantTransferRules(configs.variantTransferRules, ConfigScreen.CUSTOM_VARIANT_TRANSFER_RULE);
        ConfigScreen.updateCustomRegexTransferRules(configs.regexTransferRules, ConfigScreen.CUSTOM_REGEX_TRANSFER_RULE);

        // 从入口点导入物品组合规则。
        ITEM_COMBINATION_RULES.clear();
        // 通过入口点来获取物品组合规则。请参考 {@code fabric.mod.json} 以了解本模组使用的入口点。
        for (EntrypointContainer<Supplier<? extends Collection<? extends Map<Item, ? extends Collection<Item>>>>> entrypointContainer : FabricLoader.getInstance().getEntrypointContainers("reasonable-sorting:item_combination_rules", (Class<Supplier<? extends Collection<? extends Map<Item, ? extends Collection<Item>>>>>) (Class) Supplier.class)) {
            final Supplier<? extends Collection<? extends Map<Item, ? extends Collection<Item>>>> entrypoint = entrypointContainer.getEntrypoint();
            ITEM_COMBINATION_RULES.addAll(entrypoint.get());
        }
        LOGGER.info("%s rules are recognized!".formatted(ITEM_COMBINATION_RULES.size()));

        // 从入口点导入物品组转移规则。
        ITEM_GROUP_TRANSFER_RULES.clear();
        // 通过入口点来获取物品组转移规则。
        for (EntrypointContainer<Supplier<? extends Collection<? extends Map<Item, ItemGroup>>>> entrypointContainer : FabricLoader.getInstance().getEntrypointContainers("reasonable-sorting:item_group_transfer_rules", ((Class<Supplier<? extends Collection<? extends Map<Item, ItemGroup>>>>) (Class) Supplier.class))) {
            final Supplier<? extends Collection<? extends Map<Item, ItemGroup>>> entrypoint = entrypointContainer.getEntrypoint();
            ITEM_GROUP_TRANSFER_RULES.addAll(entrypoint.get());
        }

//        compileItemGroupTransferRules(ITEM_GROUP_TRANSFER_RULES);
    }

}
