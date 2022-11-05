package pers.solid.mod.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRule;

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
            if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer)) {
                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(_stack.getItem(), itemGroup).collect(Collectors.toSet());
                if (!groups.isEmpty()) { return groups.contains(group); }
            }
            if (search) { return ((ItemGroupInterface)group).getCachedSearchTabStacks(features, hasPermissions, false).contains(_stack); };
            return ((ItemGroupInterface)group).getCachedParentTabStacks(features, hasPermissions, false).contains(_stack);
        }
        return false;
    }

    public static ItemStackSet sorting(ItemStackSet itemStackSet, FeatureSet enabledFeatures, boolean hasPermissions) {
        return SortingRule.sortItemGroupEntries(itemStackSet);
    }

    public static ItemStackSet exclude(ItemStackSet itemStackSet, ItemGroup group, ItemGroup itemGroup, FeatureSet enabledFeatures, boolean hasPermissions) {
        // remove non-conditional transfer items
        if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR)) {
            itemStackSet.stream().forEachOrdered((stack) -> {
                if (!itemStackInGroup(stack, group, itemGroup, enabledFeatures, hasPermissions, false)) { itemStackSet.remove(stack); }
            });
        }
        return itemStackSet;
    }


    /*
    Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());

        if (!groups.isEmpty()) {
            cir.setReturnValue(groups.contains(group));
            cir.cancel();
        }
     */

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

    public static void putBefore_(List<?> collection, int indexToMoveFrom, int indexToMoveAt) {
        if (indexToMoveAt >= indexToMoveFrom) {
            Collections.rotate(collection.subList(indexToMoveFrom, indexToMoveAt + 1), -1);
        } else {
            Collections.rotate(collection.subList(indexToMoveAt, indexToMoveFrom + 1), 1);
        }
    }

    public static ItemGroup putBefore(ArrayList<ItemGroup> groups, ItemGroup beforeOf, ItemGroup el) {
        int i1 = groups.indexOf(beforeOf);
        int i2 = groups.indexOf(el);
        if (i1 >= 0 && i2 >= 0) { putBefore_(groups, i1, i2); };
        return beforeOf;
    }

    public static void transfer(ItemStackSet transferParentStacks, ItemStackSet transferSearchStacks, ItemGroup group, FeatureSet enabledFeatures, ItemGroup.Entries entries, boolean hasPermissions) {
        // back items
        ((ItemGroupEntriesInterface)entries).setParentTabStacks(transferParentStacks);
        ((ItemGroupEntriesInterface)entries).setSearchTabStacks(transferSearchStacks);

        // add conditional transfer items and remove not needed
        ArrayList<ItemGroup> groupList = new ArrayList(List.of(ItemGroups.GROUPS));
        groupList.remove(ItemGroups.HOTBAR);
        groupList.remove(ItemGroups.INVENTORY);
        groupList.remove(ItemGroups.SEARCH);
        //putBefore(groupList, ItemGroups.BUILDING_BLOCKS, ItemGroups.REDSTONE);

        // move and swap groups
/* <= */putBefore(groupList, ItemGroups.FUNCTIONAL, ItemGroups.REDSTONE); /* <= */
/* <= */putBefore(groupList, ItemGroups.TOOLS, ItemGroups.COMBAT); /* <= */

        // update cache if needed, for avoid stack overflow, and except main group
        List<ItemStack> enardParent = groupList.stream().map((itemGroup) -> ((ItemGroupInterface) itemGroup).getCachedParentTabStacks(enabledFeatures, hasPermissions, true)).distinct().flatMap(Collection::stream).distinct().toList();
        List<ItemStack> enardSearch = groupList.stream().map((itemGroup) -> ((ItemGroupInterface) itemGroup).getCachedSearchTabStacks(enabledFeatures, hasPermissions, true)).distinct().flatMap(Collection::stream).distinct().toList();

        // unify groups for correct sorting
        var stParent = new ItemStackSet(); stParent.addAll(enardParent);
        var stSearch = new ItemStackSet(); stSearch.addAll(enardSearch);

        //
        if (Configs.instance.enableSorting) {
            ItemGroupInterface.sorting(stParent, enabledFeatures, hasPermissions);
            ItemGroupInterface.sorting(stSearch, enabledFeatures, hasPermissions);
        };

        // iterate groups
        // TODO: optimize process
        ArrayList<ItemGroup> finalGroupList = groupList;//(groupList = new ArrayList(List.of(ItemGroups.GROUPS)));
        stParent.stream().forEachOrdered((stack) -> {
            finalGroupList.forEach((itemGroup) -> {
                Set<ItemGroup> groups = Configs.instance.enableGroupTransfer ? TransferRule.streamTransferredGroupOf(stack.getItem(), itemGroup).collect(Collectors.toSet()) : null;
                if ((groups == null || (!groups.isEmpty() ? groups.contains(group) : (group == itemGroup))) && itemStackInGroup(stack, group, itemGroup, enabledFeatures, hasPermissions, false)) { // now embeded
                    if (!transferParentStacks.contains(stack)) { transferParentStacks.add(stack); }
                }
            });
        });
        stSearch.stream().forEachOrdered((stack) -> {
            finalGroupList.forEach((itemGroup) -> {
                Set<ItemGroup> groups = Configs.instance.enableGroupTransfer ? TransferRule.streamTransferredGroupOf(stack.getItem(), itemGroup).collect(Collectors.toSet()) : null;
                if ((groups == null || (!groups.isEmpty() ? groups.contains(group) : (group == itemGroup))) && itemStackInGroup(stack, group, itemGroup, enabledFeatures, hasPermissions, true)) { // now embeded
                    if (!transferSearchStacks.contains(stack)) { transferSearchStacks.add(stack); }
                }
            });
        });
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
