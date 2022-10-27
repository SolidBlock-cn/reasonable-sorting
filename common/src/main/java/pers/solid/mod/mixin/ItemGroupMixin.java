package pers.solid.mod.mixin;

import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;
import pers.solid.mod.TransferRules;
import pers.solid.mod.interfaces.ItemGroupInterface;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ItemGroupInterface {
    @Shadow public abstract void addItems(FeatureSet enabledFeatures, ItemGroup.Entries entries);

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique ItemStackSet cachedSearchTabStacks = null;
    @Unique ItemStackSet cachedParentTabStacks = null;

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(@Nullable FeatureSet featureSet) {
        if (this.cachedSearchTabStacks == null) {
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(true); });
            ((ItemGroup)(Object)this).getSearchTabStacks(featureSet != null ? featureSet : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(false); });
        }
        return (this.cachedSearchTabStacks = (ItemStackSet)this.cachedSearchTabStacks.clone()); // avoid reference issues
    };

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique @Override public ItemStackSet getCachedParentTabStacks(@Nullable FeatureSet featureSet) {
        if (this.cachedParentTabStacks == null) {
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(true); });
            ((ItemGroup)(Object)this).getDisplayStacks(featureSet != null ? featureSet : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(false); });
        }
        return (this.cachedParentTabStacks = (ItemStackSet)this.cachedParentTabStacks.clone()); // avoid reference issues
    };

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique @Override public ItemStackSet getCachedSearchTabStacks() { return this.getCachedSearchTabStacks(FeatureFlags.FEATURE_MANAGER.getFeatureSet()); };
    @Unique @Override public ItemStackSet getCachedParentTabStacks() { return this.getCachedParentTabStacks(FeatureFlags.FEATURE_MANAGER.getFeatureSet()); };

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    // will no works anymore
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void isInMixin(FeatureSet enabledFeatures, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ItemGroup group = (ItemGroup) (Object) this;
        if (group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
        Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());

        if (!groups.isEmpty()) {
            cir.setReturnValue(groups.contains(group));
            cir.cancel();
        }
    }

    //
    @Unique public boolean ignoreInjection = false;

    //
    @Unique @Override public void setIgnoreInjection(boolean ignoreInjection) {
        this.ignoreInjection = ignoreInjection;
    };

    // clear cached
    @Inject(method = "clearStacks", at = @At("HEAD"))
    public void onClear(CallbackInfo ci) {
        if (!this.ignoreInjection) {
            this.cachedSearchTabStacks = null;
            this.cachedParentTabStacks = null;
        };
    };

    //
    @Inject(method = "getStacks", at = @At("RETURN"), cancellable=true)
    public void getStackInject(FeatureSet enabledFeatures, boolean search, CallbackInfoReturnable<ItemStackSet> cir) {
        final ItemStackSet itemStackSet = cir.getReturnValue();
        if ( search && this.cachedSearchTabStacks == null) { this.cachedSearchTabStacks = (ItemStackSet)itemStackSet.clone(); };
        if (!search && this.cachedParentTabStacks == null) { this.cachedParentTabStacks = (ItemStackSet)itemStackSet.clone(); };

        //
        ItemGroup group = (ItemGroup) (Object) this;

        // add conditional transfer items
        Arrays.stream(ItemGroups.GROUPS).toList().stream().forEachOrdered((itemGroup) -> {
            ((ItemGroupMixin) (Object) itemGroup).ignoreInjection = true;
            if (itemGroup == group || itemGroup == ItemGroups.INVENTORY || itemGroup == ItemGroups.SEARCH || itemGroup == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
            ((ItemGroupMixin)(Object)itemGroup).getCachedParentTabStacks(enabledFeatures).stream().forEachOrdered((stack) -> {
                if (stack == null) return;

                Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toSet());

                if (!groups.isEmpty() && groups.contains(group)) {
                    itemStackSet.add(stack); // works buggy!
                }
            });
            ((ItemGroupMixin) (Object) itemGroup).ignoreInjection = false;
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
        if ((sortedStackSet = sortedStackSet != null ? sortedStackSet : itemStackSet) != null) {
            cir.setReturnValue(sortedStackSet);
            cir.cancel();
        }
    }
}
