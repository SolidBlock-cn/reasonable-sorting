package pers.solid.mod;

import com.google.common.collect.ImmutableCollection;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MixinHelper implements ModInitializer {

    /**
     * 存放所有物品组合规则的列表。
     */
    public static final ObjectList<Map<Item, ? extends Collection<Item>>> ITEM_COMBINATION_RULES = new ObjectArrayList<>();
    public static final ObjectList<Map<Item, ItemGroup>> ITEM_GROUP_TRANSFER_RULES = new ObjectArrayList<>();
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

    public static <T> void applyItemWithFollowings(Collection<? extends Map<T, ? extends Collection<T>>> rules, T item, Consumer<T> action) {
        applyItemWithFollowings(rules, item, action,
                followingItem -> {
                    applyItemWithFollowings(rules, followingItem, action);
                }
        );
    }

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

    public void onInitialize() {
        for (EntrypointContainer<Supplier> entrypointContainer : FabricLoader.getInstance().getEntrypointContainers("reasonable-sorting:item_combination_rules", (Supplier.class))) {
            final Supplier<? extends Map<Item, ? extends Collection<Item>>> entrypoint;
            try {
                entrypoint = ((Supplier<? extends Map<Item, ? extends Collection<Item>>>) entrypointContainer.getEntrypoint());
            } catch (ClassCastException e) {
                final var e1 = entrypointContainer.getEntrypoint();
                LOGGER.fatal("Invalid entrypoint %s from mod %s. Please make sure it's an instance of Supplier<Map<Item,Collection<Item>>>.".formatted(e1, entrypointContainer.getProvider()));
                throw e;
            }
            ITEM_COMBINATION_RULES.add(entrypoint.get());
        }
        LOGGER.info("%s rules are recognized!".formatted(ITEM_COMBINATION_RULES.size()));

        for (EntrypointContainer<Supplier> entrypointContainer : FabricLoader.getInstance().getEntrypointContainers("reasonable-sorting:item_group_transfer_rules", Supplier.class)) {
            final Supplier<Map<Item, ItemGroup>> entrypoint;
            try {
                entrypoint = (Supplier<Map<Item, ItemGroup>>) entrypointContainer.getEntrypoint();
            } catch (ClassCastException e) {
                LOGGER.fatal("Invalid entrypoint %s from mod %s. Please make sure it's an instance of Supplier<Map<Item,ItemGroup>>.".formatted(entrypointContainer.getEntrypoint(), entrypointContainer.getProvider()));
                throw e;
            }
            ITEM_GROUP_TRANSFER_RULES.add(entrypoint.get());
        }

        compileItemGroupTransferRules(ITEM_GROUP_TRANSFER_RULES);
    }

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
}
