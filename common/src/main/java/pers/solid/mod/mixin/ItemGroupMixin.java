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

import javax.annotation.Nullable;
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
    @Shadow public abstract void addItems(FeatureSet enabledFeatures, ItemGroup.Entries entries);
    @Unique @Override public ItemStackSet getCachedSearchTabStacks() { return this.getCachedSearchTabStacks(null); };
    @Unique @Override public ItemStackSet getCachedParentTabStacks() { return this.getCachedParentTabStacks(null); };
    @Unique @Override public void setNeedsUpdate(boolean update) {
        this.needsToUpdate = update;
    };

    @Shadow @Override
    public ItemStackSet getDisplayStacks(@Nullable FeatureSet enabledFeatures) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getDisplayStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet);
    }

    @Shadow @Override
    public ItemStackSet getSearchTabStacks(@Nullable FeatureSet enabledFeatures) {
        var player = MinecraftClient.getInstance().player;
        var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
        return ((ItemGroup)(Object)this).getSearchTabStacks(enabledFeatures != null ? enabledFeatures : currentFeatureSet);
    }

    //
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(@Nullable FeatureSet featureSet) {
        if (this.cachedSearchTabStacks == null) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
            ((ItemGroup)(Object)this).getSearchTabStacks(featureSet != null ? featureSet : currentFeatureSet);
        }
        return this.cachedSearchTabStacks; // avoid reference issues
    }

    //
    @Unique @Override public ItemStackSet getCachedParentTabStacks(@Nullable FeatureSet featureSet) {
        if (this.cachedParentTabStacks == null) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();
            ((ItemGroup)(Object)this).getDisplayStacks(featureSet != null ? featureSet : currentFeatureSet);
        }
        return this.cachedParentTabStacks; // avoid reference issues
    }

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    // will no works anymore
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void isInMixin(FeatureSet enabledFeatures, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemGroupInterface.itemStackInGroup(stack, (ItemGroup)(Object)this, enabledFeatures));
        cir.cancel();
    }


    // clear cached
    @Inject(method = "clearStacks", at = @At("HEAD"))
    public void onClear(CallbackInfo ci) {
        this.cachedSearchTabStacks = null;
        this.cachedParentTabStacks = null;
    }

    @Inject(method = "getStacks", at = @At("HEAD"))
    public void getStackInject(FeatureSet featureSet, boolean search, CallbackInfoReturnable<ItemStackSet> cir) {
        if (this.needsToUpdate) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = featureSet != null ? featureSet : (player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet());
            if (player != null) {
                ((ItemGroupInterface) this).setDisplayStacks(null);
                ((ItemGroupInterface) this).setSearchTabStacks(null);
            }
        }
        this.needsToUpdate = false;
    }

    //
    @Redirect(method = "getStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;addItems(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;)V"))
    public void onItemAdd(ItemGroup instance, FeatureSet featureSet, ItemGroup.Entries entries) {
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
        instance.addItems(featureSet, entries);

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
            ItemGroupInterface.transfer(transferParentStacks, transferSearchStacks, instance, featureSet, entries);
        }

        //
        if (Configs.instance.enableSorting) {
            entriesInterface.setParentTabStacks(ItemGroupInterface.sorting((ItemStackSet) entriesAccessor.getParentTabStacks().clone(), instance, featureSet));
            entriesInterface.setSearchTabStacks(ItemGroupInterface.sorting((ItemStackSet) entriesAccessor.getSearchTabStacks().clone(), instance, featureSet));
        }
    }
}
