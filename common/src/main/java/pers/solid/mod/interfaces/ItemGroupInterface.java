package pers.solid.mod.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Unique;
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

    public default ItemStackSet setDisplayStacks(ItemStackSet set) { return null; };
    public default ItemStackSet setSearchTabStacks(ItemStackSet set){ return null; };
    public default void setNeedsUpdate(boolean update) { };

    //
    public static boolean itemInGroup(Item item, ItemGroup itemGroup, boolean hasPermission) {
        AtomicBoolean found = new AtomicBoolean(false);
        if (itemGroup != null) {
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(true);
            ((ItemGroupInterface)itemGroup).getCachedSearchTabStacks(false, hasPermission).forEach((stack) -> {
                if (stack != null && stack.getItem() == item) {
                    found.set(true);
                }
            });
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(false);
        }
        return found.get();
    }

    public static boolean itemStackInGroup(ItemStack _stack, ItemGroup group, ItemGroup itemGroup, boolean hasPermission) {
        if (group != null && _stack != null) {
            if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer)) {
                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(_stack.getItem(), itemGroup).collect(Collectors.toSet());
                if (!groups.isEmpty()) { return groups.contains(group); };
            }

            return (itemGroup != null ? (itemGroup == group) : (((ItemGroupInterface)group).getCachedSearchTabStacks(false, hasPermission)).contains(_stack));
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

    public static void transfer(ItemStackSet transferParentStacks, ItemStackSet transferSearchStacks, ItemGroup group, FeatureSet enabledFeatures, boolean hasPermissions) {
        // back items
        if (Configs.instance.enableGroupTransfer || Configs.instance.enableSorting) {
            // add conditional transfer items and remove not needed
            ArrayList<ItemGroup> groupList = (ArrayList<ItemGroup>)(new ArrayList(ItemGroups.getGroups()).clone());
            groupList.remove(ItemGroups.HOTBAR);
            groupList.remove(ItemGroups.INVENTORY);
            groupList.remove(ItemGroups.SEARCH);
            //groupList.add(Configs.instance.SYSTEM_ITEMS);
            //groupList = new ArrayList(groupList.stream().distinct().toList());
            //putBefore(groupList, ItemGroups.BUILDING_BLOCKS, ItemGroups.REDSTONE);

            // move and swap groups
    /* <= */putBefore(groupList, ItemGroups.FUNCTIONAL, ItemGroups.REDSTONE); /* <= */
    /* <= */putBefore(groupList, ItemGroups.REDSTONE, ItemGroups.TOOLS); /* <= */
    /* <= */putBefore(groupList, ItemGroups.TOOLS, ItemGroups.COMBAT); /* <= */

            // update cache if needed, for avoid stack overflow, and except main group
            List<Pair<ItemGroup, ItemStack>> enardParent = new ArrayList(groupList.stream().flatMap((itemGroup) -> {
                var set = ((ItemGroupInterface) itemGroup).getCachedParentTabStacks(false, hasPermissions);
                return set.stream().map((stack)->{ return new Pair<ItemGroup, ItemStack>(itemGroup, stack); }).distinct();
            }).distinct().toList());

            //
            List<Pair<ItemGroup, ItemStack>> enardSearch = new ArrayList(groupList.stream().flatMap((itemGroup) -> {
                var set = ((ItemGroupInterface) itemGroup).getCachedSearchTabStacks(false, hasPermissions);
                return set.stream().map((stack)->{ return new Pair<ItemGroup, ItemStack>(itemGroup, stack); }).distinct();
            }).distinct().toList());

            //
            if (Configs.instance.enableSorting) {
                ItemGroupInterface.sorting(enardParent, enabledFeatures, hasPermissions);
                ItemGroupInterface.sorting(enardSearch, enabledFeatures, hasPermissions);
            };

            // iterate groups
            enardParent.stream().forEachOrdered((stack) -> {
                if (itemStackInGroup(stack.getRight(), group, stack.getLeft(), hasPermissions)) { // now embeded
                    if (!transferParentStacks.contains(stack.getRight())) {
                        transferParentStacks.add(stack.getRight());
                    }
                }
            });

            //
            enardSearch.stream().forEachOrdered((stack) -> {
                if (itemStackInGroup(stack.getRight(), group, stack.getLeft(), hasPermissions)) { // now embeded
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
            ArrayList<ItemGroup> groupList = (ArrayList)(new ArrayList(ItemGroups.getGroups()).clone());
            groupList.remove(ItemGroups.HOTBAR);
            groupList.remove(ItemGroups.INVENTORY);
            groupList.remove(ItemGroups.SEARCH);
            //groupList.add(Configs.instance.SYSTEM_ITEMS);
            //groupList = new ArrayList(groupList.stream().distinct().toList());

            //
            groupList.stream().forEachOrdered((g) -> {
                if (group == null || group != g) {
                    ((ItemGroupInterface) g).setDisplayStacks(null);
                    ((ItemGroupInterface) g).setSearchTabStacks(null);
                }
            });

            //
            groupList.stream().forEachOrdered((g) -> {
                if (group == null || group != g) {
                    ((ItemGroupInterface) g).getSearchTabStacks();
                    ((ItemGroupInterface) g).getDisplayStacks();
                }
            });
        }
    }

    public default ItemStackSet getDisplayStacks() {
        return ((ItemGroup)(Object)this).getDisplayStacks();
    }

    public default ItemStackSet getSearchTabStacks() {
        return ((ItemGroup)(Object)this).getSearchTabStacks();
    }

    //
    ItemStackSet getCachedSearchTabStacks(boolean needsUpdate, boolean hasPermissions);
    ItemStackSet getCachedParentTabStacks(boolean needsUpdate, boolean hasPermissions);

    //
    public ItemStackSet setCachedSearchTabStacks(ItemStackSet stackSet);
    public ItemStackSet setCachedParentTabStacks(ItemStackSet stackSet);
}
