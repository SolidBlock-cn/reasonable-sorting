package pers.solid.mod.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRule;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface ItemGroupInterface {
    public default ItemStackSet getCachedSearchTabStacks(boolean hasPermissions, boolean needsUpdate) { return null; };
    public default ItemStackSet getCachedParentTabStacks(boolean hasPermissions, boolean needsUpdate) { return null; };
    public default ItemStackSet getCachedSearchTabStacks(FeatureSet featureSet, boolean hasPermissions, boolean needsUpdate) { return null; };
    public default ItemStackSet getCachedParentTabStacks(FeatureSet featureSet, boolean hasPermissions, boolean needsUpdate) { return null; };
    public default ItemStackSet getDisplayStacks() {
        return null;
    }
    public default ItemStackSet getSearchTabStacks() {
        return null;
    }

    public default void setDisplayStacks(ItemStackSet set) {  };
    public default void setSearchTabStacks(ItemStackSet set){  };
    public default void setNeedsUpdate(boolean update) { };


    public static boolean itemInGroup(Item item, ItemGroup itemGroup, FeatureSet features, boolean hasPermissions) {
        AtomicBoolean found = new AtomicBoolean(false);
        if (itemGroup != null) {
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(true);
            ((ItemGroupInterface)itemGroup).getCachedSearchTabStacks(features, hasPermissions, false).forEach((stack) -> {
                if (stack != null && stack.getItem() == item) {
                    found.set(true);
                }
            });
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(false);
        }
        return found.get();
    }

    public static boolean itemInGroup(Item item, ItemGroup itemGroup, boolean hasPermissions) {
        return itemInGroup(item, itemGroup, null, hasPermissions);
    }

    public static boolean itemInGroup(Item item, ItemGroup itemGroup) {
        var player = MinecraftClient.getInstance().player;
        return itemInGroup(item, itemGroup, player != null ? player.hasPermissionLevel(1) : false);
    }

    public static boolean itemStackInGroup(ItemStack _stack, ItemGroup group, ItemGroup itemGroup, FeatureSet features, boolean hasPermissions, boolean search) {
        if (group != null && _stack != null) {
            var inStackSet = (search ?
                    ((ItemGroupInterface)group).getCachedSearchTabStacks(features, hasPermissions, false) :
                    ((ItemGroupInterface)group).getCachedParentTabStacks(features, hasPermissions, false)).contains(_stack);

            if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer)) {
                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(_stack.getItem(), itemGroup).collect(Collectors.toSet());
                if (!groups.isEmpty()) { return groups.contains(group); };
            }

            return inStackSet && (itemGroup != null ? (itemGroup == group) : true);
        }
        return false;
    }

    public static List<Pair<ItemGroup, ItemStack>> sorting(List<Pair<ItemGroup, ItemStack>> itemStackSet, FeatureSet enabledFeatures, boolean hasPermissions) {
        return SortingRule.sortItemGroupEntries(itemStackSet);
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }

    public static void swap(ArrayList<ItemGroup> groups, ItemGroup g1, ItemGroup g2) {
        int i1 = groups.indexOf(g1);
        int i2 = groups.indexOf(g2);
        if (i1 >= 0 && i2 >= 0) { Collections.swap(groups, i1, i2); };
    }


    public static <T> List<T> splice(List<T> list, int index, int deleteCount) {
        return spliceImpl(list, index, deleteCount, false, null);
    }

    /**
     * Removes n elements found at the specified index. And then inserts the
     * specified item at the index
     *
     * @param list elements
     * @param index
     *         index at which we are inserting/removing
     * @param deleteCount
     *         the number of elements we will remove starting at the index
     * @param value
     *         the item we want to add to the array at the index
     * @return an array of elements that were removed
     */
    public static <T> List<T> splice(List<T> list, int index, int deleteCount, T value) {
        return spliceImpl(list, index, deleteCount, true, value);
    }

    private static <T> List<T> spliceImpl(List<T> list, int index, int deleteCount, boolean hasValue, T value) {
        List<T> removedArray = new ArrayList<>();
        for (int i = deleteCount; i > 0; i--) {
            T removedElem = list.remove(index);
            removedArray.add(removedElem);
        }

        if (hasValue) {
            list.add(index, value);
        }

        return removedArray;
    }
    public static ItemGroup putBefore(ArrayList<ItemGroup> arr, ItemGroup beforeOf, ItemGroup el) {
        int new_index = arr.indexOf(beforeOf);
        int old_index = arr.indexOf(el);
        if (new_index >= 0 && old_index >= 0) {
            var array = (List<ItemGroup>)arr.clone(); arr.clear();
            var spliced = splice((List<ItemGroup>)array, old_index, 1).get(0);
            splice((List<ItemGroup>)array, new_index, 0, spliced); arr.addAll((List)array);
            return spliced;
        }
        return beforeOf;
    }

    public static void transfer(ItemStackSet transferParentStacks, ItemStackSet transferSearchStacks, ItemGroup group, FeatureSet enabledFeatures, ItemGroup.Entries entries, boolean hasPermissions) {
        // back items
        if (Configs.instance.enableGroupTransfer || Configs.instance.enableSorting) {
            ((ItemGroupEntriesInterface) entries).setParentTabStacks(transferParentStacks);
            ((ItemGroupEntriesInterface) entries).setSearchTabStacks(transferSearchStacks);

            // add conditional transfer items and remove not needed
            ArrayList<ItemGroup> groupList = new ArrayList(List.of(ItemGroups.GROUPS));
            groupList.remove(ItemGroups.HOTBAR);
            groupList.remove(ItemGroups.INVENTORY);
            groupList.remove(ItemGroups.SEARCH);
            //putBefore(groupList, ItemGroups.BUILDING_BLOCKS, ItemGroups.REDSTONE);

            // move and swap groups
    /* <= */putBefore(groupList, ItemGroups.FUNCTIONAL, ItemGroups.REDSTONE); /* <= */
    /* <= */putBefore(groupList, ItemGroups.REDSTONE, ItemGroups.TOOLS); /* <= */
    /* <= */putBefore(groupList, ItemGroups.TOOLS, ItemGroups.COMBAT); /* <= */

            // update cache if needed, for avoid stack overflow, and except main group
            List<Pair<ItemGroup, ItemStack>> enardParent = new ArrayList(groupList.stream().flatMap((itemGroup) -> {
                var set = ((ItemGroupInterface) itemGroup).getCachedParentTabStacks(enabledFeatures, hasPermissions, true);
                return set.stream().map((stack)->{ return new Pair<ItemGroup, ItemStack>(itemGroup, stack); }).distinct();
            }).distinct().toList());

            //
            List<Pair<ItemGroup, ItemStack>> enardSearch = new ArrayList(groupList.stream().flatMap((itemGroup) -> {
                var set = ((ItemGroupInterface) itemGroup).getCachedSearchTabStacks(enabledFeatures, hasPermissions, true);
                return set.stream().map((stack)->{ return new Pair<ItemGroup, ItemStack>(itemGroup, stack); }).distinct();
            }).distinct().toList());

            //
            if (Configs.instance.enableSorting) {
                ItemGroupInterface.sorting(enardParent, enabledFeatures, hasPermissions);
                ItemGroupInterface.sorting(enardSearch, enabledFeatures, hasPermissions);
            };

            // iterate groups
            enardParent.stream().forEachOrdered((stack) -> {
                if (itemStackInGroup(stack.getRight(), group, stack.getLeft(), enabledFeatures, hasPermissions, false)) { // now embeded
                    if (!transferParentStacks.contains(stack.getRight())) {
                        transferParentStacks.add(stack.getRight());
                    }
                }
            });

            //
            enardSearch.stream().forEachOrdered((stack) -> {
                if (itemStackInGroup(stack.getRight(), group, stack.getLeft(), enabledFeatures, hasPermissions, true)) { // now embeded
                    if (!transferSearchStacks.contains(stack.getRight())) {
                        transferSearchStacks.add(stack.getRight());
                    }
                }
            });
        }
    }

    //
    public static void updateGroups(FeatureSet featureSet, ItemGroup group, boolean hasPermissions) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = featureSet != null ? featureSet : (player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
        if (player != null) {
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((g) -> {
                if ((group == null || group != g) && !(g == ItemGroups.INVENTORY || g == ItemGroups.SEARCH || g == ItemGroups.HOTBAR)) {
                    ((ItemGroupInterface) g).setDisplayStacks(null);
                    ((ItemGroupInterface) g).setSearchTabStacks(null);
                }
            });
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((g) -> {
                if ((group == null || group != g) && !(g == ItemGroups.INVENTORY || g == ItemGroups.SEARCH || g == ItemGroups.HOTBAR)) {
                    ((ItemGroupInterface) g).getSearchTabStacks(currentFeatureSet, hasPermissions);
                    ((ItemGroupInterface) g).getDisplayStacks(currentFeatureSet, hasPermissions);
                }
            });
        }
    }

    public default ItemStackSet getDisplayStacks(FeatureSet enabledFeatures, boolean hasPermissions) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getDisplayStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet, hasPermissions);
    }

    public default ItemStackSet getSearchTabStacks(FeatureSet enabledFeatures, boolean hasPermissions) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getSearchTabStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet, hasPermissions);
    }

}
