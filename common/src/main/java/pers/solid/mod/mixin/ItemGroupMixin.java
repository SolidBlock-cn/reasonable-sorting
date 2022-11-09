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
import pers.solid.mod.*;
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
    @Unique boolean avoidMixin = false;

    //
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(boolean hasPermissions, boolean needsToUpdate) { return this.getCachedSearchTabStacks(null, hasPermissions, needsToUpdate); };
    @Unique @Override public ItemStackSet getCachedParentTabStacks(boolean hasPermissions, boolean needsToUpdate) { return this.getCachedParentTabStacks(null, hasPermissions, needsToUpdate); };
    @Unique @Override public void setNeedsUpdate(boolean update) {
        this.needsToUpdate = update;
    };

    //
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(FeatureSet featureSet, boolean hasPermissions, boolean needsUpdate) {
        if (this.cachedSearchTabStacks == null) {
            if (needsUpdate) {
                var player = MinecraftClient.getInstance().player;
                var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
                ((ItemGroup) (Object) this).getSearchTabStacks(featureSet != null ? featureSet : currentFeatureSet, hasPermissions);
            }
        }
        return (ItemStackSet)this.cachedSearchTabStacks.clone(); // avoid reference issues
    }

    //
    @Unique @Override public ItemStackSet getCachedParentTabStacks(FeatureSet featureSet, boolean hasPermissions, boolean needsUpdate) {
        if (this.cachedParentTabStacks == null) {
            if (needsUpdate) {
                var player = MinecraftClient.getInstance().player;
                var currentFeatureSet = player != null ? player.networkHandler.getEnabledFeatures() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
                ((ItemGroup) (Object) this).getDisplayStacks(featureSet != null ? featureSet : currentFeatureSet, hasPermissions);
            }
        }
        return (ItemStackSet)this.cachedParentTabStacks.clone(); // avoid reference issues
    }

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    // will no works anymore
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void isInMixin(FeatureSet enabledFeatures, ItemStack stack, boolean hasPermissions, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemGroupInterface.itemStackInGroup(stack, (ItemGroup)(Object)this, null, enabledFeatures, hasPermissions, true));
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

    @Inject(method = "getStacks", at = @At("HEAD"))
    public void cancelMixin(FeatureSet enabledFeatures, boolean search, boolean hasPermissions, CallbackInfoReturnable<ItemStackSet> cir) {

    }

    //
    @Redirect(method = "getStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;addItems(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;Z)V"))
    public void onItemAdd(ItemGroup instance, FeatureSet featureSet, ItemGroup.Entries entries, boolean hasPermissions) {
        // interface
        var entriesInterface = ((ItemGroupEntriesInterface) entries);
        var entriesAccessor = (ItemGroupEntriesImplAccessor) entries;

        //
        ((ItemGroup)(Object)this).addItems(featureSet, entries, hasPermissions);

        //
        var originalParentStacksRef = (ItemStackSet)entriesAccessor.getParentTabStacks();
        var originalSearchStacksRef = (ItemStackSet)entriesAccessor.getSearchTabStacks();

        // reference, from empty
        var emptyParentStacksRef = (this.cachedParentTabStacks != null ? this.cachedParentTabStacks : (this.cachedParentTabStacks = (ItemStackSet)(!avoidMixin ? (ItemStackSet)entriesAccessor.getParentTabStacks() : null).clone()).clone());
        var emptySearchStacksRef = (this.cachedSearchTabStacks != null ? this.cachedSearchTabStacks : (this.cachedSearchTabStacks = (ItemStackSet)(!avoidMixin ? (ItemStackSet)entriesAccessor.getSearchTabStacks() : null).clone()).clone());

        //
        emptyParentStacksRef.clear();
        emptySearchStacksRef.clear();

        //
        if (!(instance == ItemGroups.INVENTORY || instance == ItemGroups.SEARCH || instance == ItemGroups.HOTBAR)) {
            // transfer and sorting
            ItemGroupInterface.transfer((ItemStackSet) emptyParentStacksRef, (ItemStackSet) emptySearchStacksRef, instance, featureSet, entries, hasPermissions);

            //
            entriesAccessor.setParentTabStacks((ItemStackSet) emptyParentStacksRef);
            entriesAccessor.setSearchTabStacks((ItemStackSet) emptySearchStacksRef);
        }
    }
}
