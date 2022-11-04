package pers.solid.mod.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;

import java.util.*;
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
        if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR)) {
            itemStackSet.stream().forEachOrdered((stack) -> {
                if (!itemStackInGroup(stack, group, enabledFeatures)) { itemStackSet.remove(stack); }
            });
        }
        return itemStackSet;
    }

    public static boolean itemStackInGroup(ItemStack _stack, ItemGroup itemGroup, FeatureSet features) {
        if (itemGroup != null && _stack != null) {
            if (!(itemGroup == ItemGroups.INVENTORY || itemGroup == ItemGroups.SEARCH || itemGroup == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer)) {
                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(_stack.getItem()).collect(Collectors.toSet());
                if (!groups.isEmpty()) { return groups.contains(itemGroup); }
            }
            return ((ItemGroupInterface)itemGroup).getCachedSearchTabStacks(features).contains(_stack);
        }
        return false;
    }

    /*
    Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());

        if (!groups.isEmpty()) {
            cir.setReturnValue(groups.contains(group));
            cir.cancel();
        }
     */

    public static boolean itemInGroup(Item item, ItemGroup itemGroup, FeatureSet features) {
        AtomicBoolean found = new AtomicBoolean(false);
        if (itemGroup != null) {
            //((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(true);
            ((ItemGroupInterface)itemGroup).getCachedSearchTabStacks(features).forEach((stack) -> {
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

    public static void transfer(ItemStackSet transferParentStacks, ItemStackSet transferSearchStacks, ItemGroup group, FeatureSet enabledFeatures, ItemGroup.Entries entries) {
        var cachedParent = (ItemStackSet)transferParentStacks.clone();
        var cachedSearch = (ItemStackSet)transferSearchStacks.clone();

        // add items from original groups
        ((ItemGroupEntriesInterface)entries).setParentTabStacks(cachedParent);
        ((ItemGroupEntriesInterface)entries).setSearchTabStacks(cachedSearch);
        group.addItems(enabledFeatures, entries);

        // set transferred reference
        ((ItemGroupEntriesInterface)entries).setParentTabStacks(transferParentStacks);
        ((ItemGroupEntriesInterface)entries).setSearchTabStacks(transferSearchStacks);

        // add conditional transfer items
        ArrayList<ItemGroup> groupList = new ArrayList(List.of(ItemGroups.GROUPS));

        // remove not needed
        groupList.remove(ItemGroups.HOTBAR);
        groupList.remove(ItemGroups.INVENTORY);
        groupList.remove(ItemGroups.SEARCH);

        // correct order for defaults
        //groupList.remove(group);
        //putBefore(groupList, ItemGroups.BUILDING_BLOCKS,
            //putBefore(groupList, ItemGroups.FUNCTIONAL,
                //putBefore(groupList, ItemGroups.REDSTONE,
                    //putBefore(groupList, ItemGroups.TOOLS, ItemGroups.COMBAT))));
        groupList = new ArrayList(reverse(groupList.stream()).toList());
        //groupList.add(group);

        // iterate groups
        groupList.forEach((itemGroup) -> {
            var stream =
                //SortingRule.streamOfRegistry(Registry.ITEM_KEY, ((ItemGroupInterface)itemGroup).getCachedParentTabStacks(enabledFeatures))
                itemGroup == group ? cachedSearch.stream() : ((ItemGroupInterface) itemGroup).getCachedSearchTabStacks(enabledFeatures).clone().stream();

            //
            stream.forEachOrdered((stack) -> {
                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());
                if ((!groups.isEmpty() ? groups.contains(group) : (group == itemGroup)) && itemStackInGroup(stack, group, enabledFeatures)) { // now embeded
                    if (!transferParentStacks.contains(stack)) {
                        transferParentStacks.add(stack);
                    }
                    if (!transferSearchStacks.contains(stack)) {
                        transferSearchStacks.add(stack);
                    }
                }
            });
        });
    }

    public static void updateGroups(FeatureSet featureSet, ItemGroup group) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = featureSet != null ? featureSet : (player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
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

    public default ItemStackSet getDisplayStacks(FeatureSet enabledFeatures) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getDisplayStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet);
    }

    public default ItemStackSet getSearchTabStacks(FeatureSet enabledFeatures) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getSearchTabStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet);
    }

}
