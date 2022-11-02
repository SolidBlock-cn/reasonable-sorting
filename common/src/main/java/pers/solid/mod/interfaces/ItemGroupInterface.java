package pers.solid.mod.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
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
        return (ItemStackSet)(sortedStackSet != null ? sortedStackSet : itemStackSet).clone();
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
        return (ItemStackSet)itemStackSet.clone();
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }

    public static void transfer(ItemStackSet itemStackSet[], ItemGroup group, FeatureSet enabledFeatures, ItemGroup.Entries entries) {
        // add conditional transfer items
        reverse(Arrays.stream(ItemGroups.GROUPS).toList().stream()).forEachOrdered((itemGroup) -> {
            if (itemGroup != group) {
                if (itemGroup == ItemGroups.INVENTORY || itemGroup == ItemGroups.SEARCH || itemGroup == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
                ((ItemGroupInterface) (Object) itemGroup).getCachedParentTabStacks(enabledFeatures).stream().forEachOrdered((stack) -> {
                    if (stack == null) return;

                    Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());
                    if (groups.contains(group)) {
                        itemStackSet[0].add(stack);
                        itemStackSet[1].add(stack);
                    }
                });
            } else {
                group.addItems(enabledFeatures, entries);
            }
        });
        //return (ItemStackSet)itemStackSet.clone();
    }

    public static void updateGroups(@Nullable FeatureSet featureSet, @Nullable ItemGroup group) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = featureSet != null ? featureSet : (player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
        if (player != null) {
            Arrays.stream(ItemGroups.GROUPS).forEach((g) -> {
                if (g == ItemGroups.INVENTORY || g == ItemGroups.SEARCH || g == ItemGroups.HOTBAR) return;
                if (group == null || group != g) {
                    ((ItemGroupInterface) g).setDisplayStacks(null);
                    ((ItemGroupInterface) g).setSearchTabStacks(null);
                }
            });

            Arrays.stream(ItemGroups.GROUPS).forEach((g) -> {
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
