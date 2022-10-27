package pers.solid.mod.interfaces;

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
import java.util.Set;
import java.util.stream.Collectors;

public interface ItemGroupInterface {

    public static ItemStackSet sortingAndTransfer(ItemStackSet itemStackSet, ItemGroup group, FeatureSet enabledFeatures) {
        // add conditional transfer items
        Arrays.stream(ItemGroups.GROUPS).toList().stream().forEachOrdered((itemGroup) -> {
            ((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(true);
            if (itemGroup == group || itemGroup == ItemGroups.INVENTORY || itemGroup == ItemGroups.SEARCH || itemGroup == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
            ((ItemGroupInterface)(Object)itemGroup).getCachedParentTabStacks(enabledFeatures).stream().forEachOrdered((stack) -> {
                if (stack == null) return;

                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());

                if (!groups.isEmpty() && groups.contains(group)) {
                    itemStackSet.add(stack); // works buggy!
                }
            });
            ((ItemGroupInterface) (Object) itemGroup).setIgnoreInjection(false);
        });

        // remove non-conditional transfer items
        if (!(group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer)) {
            itemStackSet.stream().forEachOrdered((stack) -> {
                if (stack != null) {
                    Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());

                    if (!groups.isEmpty() && !groups.contains(group)) {
                        itemStackSet.remove(stack);
                    }
                }
            });
        }

        //
        var sortedStackSet = SortingRule.sortItemGroupEntries(itemStackSet);
        return (ItemStackSet)(sortedStackSet != null ? sortedStackSet : itemStackSet).clone();
    }

    public default ItemStackSet getDisplayStacks() {
        return null;
    }

    public default ItemStackSet getSearchTabStacks() {
        return null;
    }

    public default ItemStackSet getDisplayStacks(@Nullable FeatureSet enabledFeatures) {
        return ((ItemGroup)(Object)this).getDisplayStacks(enabledFeatures != null ? enabledFeatures : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
    }

    public default ItemStackSet getSearchTabStacks(@Nullable FeatureSet enabledFeatures) {
        return ((ItemGroup)(Object)this).getSearchTabStacks(enabledFeatures != null ? enabledFeatures : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
    }

    public void setIgnoreInjection(boolean ignoreInjection);
    public default ItemStackSet getCachedSearchTabStacks() { return null; };
    public default ItemStackSet getCachedParentTabStacks() { return null; };
    public default ItemStackSet getCachedSearchTabStacks(FeatureSet featureSet) { return null; };
    public default ItemStackSet getCachedParentTabStacks(FeatureSet featureSet) { return null; };
}
