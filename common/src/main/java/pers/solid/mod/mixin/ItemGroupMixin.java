package pers.solid.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;
import pers.solid.mod.TransferRules;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;
import pers.solid.mod.interfaces.ItemGroupInterface;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ItemGroupInterface {

    // 
    @Unique ItemStackSet cachedSearchTabStacks = null;
    @Unique ItemStackSet cachedParentTabStacks = null;
    @Unique boolean needsToUpdate = false;

    //
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(boolean hasPermissions) { return this.getCachedSearchTabStacks(null, hasPermissions); };
    @Unique @Override public ItemStackSet getCachedParentTabStacks(boolean hasPermissions) { return this.getCachedParentTabStacks(null, hasPermissions); };
    @Unique @Override public void setNeedsUpdate(boolean update) {
        this.needsToUpdate = update;
    };

    @Override
    public ItemStackSet getDisplayStacks(FeatureSet enabledFeatures, boolean hasPermissions) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getDisplayStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet, hasPermissions);
    }

    @Override
    public ItemStackSet getSearchTabStacks(FeatureSet enabledFeatures, boolean hasPermissions) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getSearchTabStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet, hasPermissions);
    }

    //
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(FeatureSet featureSet, boolean hasPermissions) {
        if (this.cachedSearchTabStacks == null) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
            ((ItemGroup)(Object)this).getSearchTabStacks(featureSet != null ? featureSet : currentFeatureSet, hasPermissions);
        }
        return this.cachedSearchTabStacks; // avoid reference issues
    }

    //
    @Unique @Override public ItemStackSet getCachedParentTabStacks(FeatureSet featureSet, boolean hasPermissions) {
        if (this.cachedParentTabStacks == null) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
            ((ItemGroup)(Object)this).getDisplayStacks(featureSet != null ? featureSet : currentFeatureSet, hasPermissions);
        }
        return this.cachedParentTabStacks; // avoid reference issues
    }

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    // will no works anymore
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void isInMixin(FeatureSet enabledFeatures, ItemStack stack, boolean hasPermissions, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemGroupInterface.itemStackInGroup(stack, (ItemGroup)(Object)this, null, enabledFeatures, hasPermissions));
        cir.cancel();
    }


    // clear cached
    //@Inject(method = "clearStacks", at = @At("HEAD"))
    //public void onClear(boolean hasPermissions, CallbackInfo ci) {
        //this.cachedSearchTabStacks = null;
        //this.cachedParentTabStacks = null;
    //}

    @Inject(method = "getStacks", at = @At("HEAD"))
    public void getStackInject(FeatureSet featureSet, boolean search, boolean hasPermissions, CallbackInfoReturnable<ItemStackSet> cir) {
        if (this.needsToUpdate) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = featureSet != null ? featureSet : (player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
            if (player != null) {
                ((ItemGroupInterface) this).setDisplayStacks(null);
                ((ItemGroupInterface) this).setSearchTabStacks(null);
            }
        }
        this.needsToUpdate = false;
    }

    //
    @Redirect(method = "getStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;addItems(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;Z)V"))
    public void onItemAdd(ItemGroup instance, FeatureSet featureSet, ItemGroup.Entries entries, boolean hasPermissions) {
        // interface
        var entriesInterface = ((ItemGroupEntriesInterface) entries);
        var entriesAccessor = (ItemGroupEntriesImplAccessor) entries;

        // reference, from empty
        var originalParentStacksRef = (ItemStackSet)entriesAccessor.getParentTabStacks();
        var originalSearchStacksRef = (ItemStackSet)entriesAccessor.getSearchTabStacks();

        //
        var transferParentStacks = (ItemStackSet)originalParentStacksRef.clone();
        var transferSearchStacks = (ItemStackSet)originalSearchStacksRef.clone();

        // system function, add to original...Ref
        instance.addItems(featureSet, entries, hasPermissions);

        // only after added items
        if (this.cachedParentTabStacks == null) {
            this.cachedParentTabStacks = (ItemStackSet)originalParentStacksRef.clone();
        }

        // only after added items
        if (this.cachedSearchTabStacks == null) {
            this.cachedSearchTabStacks = (ItemStackSet)originalSearchStacksRef.clone();
        }

        //
        if (instance == ItemGroups.INVENTORY || instance == ItemGroups.SEARCH || instance == ItemGroups.HOTBAR) return;

        // transfer and sorting
        if (Configs.instance.enableGroupTransfer) {
            ItemGroupInterface.transfer(transferParentStacks, transferSearchStacks, instance, featureSet, entries, hasPermissions);
        }

        //
        if (Configs.instance.enableSorting) {
            entriesInterface.setParentTabStacks(ItemGroupInterface.sorting((ItemStackSet) entriesAccessor.getParentTabStacks().clone(), instance, null, featureSet, hasPermissions));
            entriesInterface.setSearchTabStacks(ItemGroupInterface.sorting((ItemStackSet) entriesAccessor.getSearchTabStacks().clone(), instance, null, featureSet, hasPermissions));
        }
    }
}
