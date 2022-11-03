package pers.solid.mod.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface ItemGroupInterface {
    public default ItemStackSet getCachedSearchTabStacks() { return null; };
    public default ItemStackSet getCachedParentTabStacks() { return null; };
    public default ItemStackSet getCachedSearchTabStacks(FeatureSet featureSet) { return null; };
    public default ItemStackSet getCachedParentTabStacks(FeatureSet featureSet) { return null; };
    public default ItemStackSet getDisplayStacks() {
        return null;
    }
    public default ItemStackSet getSearchTabStacks() {
        return null;
    }

    public default void setDisplayStacks(ItemStackSet set) {  };
    public default void setSearchTabStacks(ItemStackSet set){  };
    public default void setNeedsUpdate(boolean update) { };

    public static ItemStackSet sorting(ItemStackSet itemStackSet, ItemGroup group, FeatureSet enabledFeatures) {
        var sortedStackSet = SortingRule.sortItemGroupEntries(itemStackSet);
        return (sortedStackSet != null ? sortedStackSet : itemStackSet);
    }

    public static ItemStackSet exclude(ItemStackSet itemStackSet, ItemGroup group, FeatureSet enabledFeatures) {
        // remove non-conditional transfer items
        if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer)) {
            itemStackSet.stream().forEachOrdered((stack) -> {
                if (stack != null) {
                    Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());
                    if (!groups.isEmpty() && !groups.contains(group)) { itemStackSet.remove(stack); }
                }
            });
        }
        return itemStackSet;
    }

    public static boolean itemStackInGroup(ItemStack _stack, ItemGroup itemGroup, @Nullable FeatureSet features) {
        AtomicBoolean found = new AtomicBoolean(false);
        if (itemGroup != null) {
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(true);
            return ((ItemGroupInterface)itemGroup).getCachedParentTabStacks(features).contains(_stack);
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(false);
        }
        return found.get();
    }

    public static boolean itemInGroup(Item item, ItemGroup itemGroup, @Nullable FeatureSet features) {
        AtomicBoolean found = new AtomicBoolean(false);
        if (itemGroup != null) {
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(true);
            ((ItemGroupInterface)itemGroup).getCachedParentTabStacks(features).forEach((stack) -> {
                if (stack != null && stack.getItem() == item) {
                    found.set(true);
                }
            });
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(false);
        }
        return found.get();
    }

    public static boolean itemInGroup(Item item, ItemGroup itemGroup) {
        return itemInGroup(item, itemGroup, null);
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }

    public static void transfer(ItemStackSet transferParentStacks, ItemStackSet transferSearchStacks, ItemGroup group, FeatureSet enabledFeatures, ItemGroup.Entries entries) {
        // add conditional transfer items

        reverse(Arrays.stream(ItemGroups.GROUPS).toList().stream()).forEachOrdered((itemGroup) -> {
            if (itemGroup != group) {
                if (itemGroup == ItemGroups.INVENTORY || itemGroup == ItemGroups.SEARCH || itemGroup == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
                //SortingRule.streamOfRegistry(Registry.ITEM_KEY, ((ItemGroupInterface)itemGroup).getCachedParentTabStacks(enabledFeatures))
                ((ItemGroupInterface)itemGroup).getCachedParentTabStacks(enabledFeatures).stream()
                .forEachOrdered((stack) -> {
                    if (stack == null) return;
                    Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());
                    if (groups.contains(group) && !itemStackInGroup(stack, group, enabledFeatures)) {
                        transferParentStacks.add(stack);
                        transferSearchStacks.add(stack);
                    }
                });
            } else {
                // current group
                ((ItemGroupEntriesInterface)entries).setParentTabStacks(transferParentStacks);
                ((ItemGroupEntriesInterface)entries).setSearchTabStacks(transferSearchStacks);
                group.addItems(enabledFeatures, entries);
                exclude(transferParentStacks, group, enabledFeatures);
                exclude(transferSearchStacks, group, enabledFeatures);
            }
        });
    }

    public static void updateGroups(@Nullable FeatureSet featureSet, @Nullable ItemGroup group) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = featureSet != null ? featureSet : (player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
        if (player != null) {
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((g) -> {
                if (g == ItemGroups.INVENTORY || g == ItemGroups.SEARCH || g == ItemGroups.HOTBAR) return;
                if (group == null || group != g) {
                    ((ItemGroupInterface) g).setDisplayStacks(null);
                    ((ItemGroupInterface) g).setSearchTabStacks(null);
                }
            });

            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((g) -> {
                if (g == ItemGroups.INVENTORY || g == ItemGroups.SEARCH || g == ItemGroups.HOTBAR) return;
                if (group == null || group != g) {
                    ((ItemGroupInterface) g).getDisplayStacks(currentFeatureSet);
                }
            });
        }
    }

    public default ItemStackSet getDisplayStacks(@Nullable FeatureSet enabledFeatures) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getDisplayStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet);
    }

    public default ItemStackSet getSearchTabStacks(@Nullable FeatureSet enabledFeatures) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getSearchTabStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet);
    }

}
