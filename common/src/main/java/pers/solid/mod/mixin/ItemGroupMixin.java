package pers.solid.mod.mixin;

import net.minecraft.client.MinecraftClient;
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
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;
import pers.solid.mod.interfaces.ItemGroupInterface;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ItemGroupInterface {

    @Shadow private ItemStackSet searchTabStacks;
    @Shadow private ItemStackSet displayStacks;
    //
    @Unique ItemStackSet cachedSearchTabStacks = null;
    @Unique ItemStackSet cachedParentTabStacks = null;

    @Unique boolean needsUpdate = false;

    //
    @Unique @Override public boolean getNeedsUpdate() { return needsUpdate; };
    @Unique @Override public void setNeedsUpdate(boolean N) { needsUpdate = N; };

    //
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(boolean needsUpdate, boolean hasPermissions) {
        return this.cachedSearchTabStacks != null ? (ItemStackSet)this.cachedSearchTabStacks.clone() : null; // avoid reference issues
    }

    //
    @Unique @Override public ItemStackSet getCachedParentTabStacks(boolean needsUpdate, boolean hasPermissions) {
        return this.cachedParentTabStacks != null ? (ItemStackSet)this.cachedParentTabStacks.clone() : null; // avoid reference issues
    }

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    // will no works anymore
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void isInMixin(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemGroupInterface.itemStackInGroup(stack, (ItemGroup)(Object)this, null, true));
        cir.cancel();
    }

    //
    @Redirect(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup$EntryCollector;accept(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;Z)V"))
    public void onItemAdd(ItemGroup.EntryCollector instance, FeatureSet featureSet, ItemGroup.Entries entries, boolean b) {
        this.forceUpdate();
        instance.accept(featureSet, entries, b);
    }

    //
    @Unique @Override public ItemStackSet setCachedSearchTabStacks(ItemStackSet stackSet) { return (this.cachedSearchTabStacks = stackSet); };
    @Unique @Override public ItemStackSet setCachedParentTabStacks(ItemStackSet stackSet) { return (this.cachedParentTabStacks = stackSet); };

    //
    @Unique @Override public ItemStackSet setSearchTabStacks(ItemStackSet stackSet) { return (this.searchTabStacks = stackSet); };
    @Unique @Override public ItemStackSet setDisplayStacks(ItemStackSet stackSet) { return (this.displayStacks = stackSet); };
}
