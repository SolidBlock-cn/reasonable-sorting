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
    @Shadow public abstract void addItems(FeatureSet enabledFeatures, ItemGroup.Entries entries);

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique ItemStackSet cachedSearchTabStacks = null;
    @Unique ItemStackSet cachedParentTabStacks = null;

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

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique @Override public ItemStackSet getCachedSearchTabStacks(@Nullable FeatureSet featureSet) {
        if (this.cachedSearchTabStacks == null) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();

            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(true); });
            ((ItemGroup)(Object)this).getSearchTabStacks(featureSet != null ? featureSet : currentFeatureSet);
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(false); });
        }
        return (this.cachedSearchTabStacks = (ItemStackSet)this.cachedSearchTabStacks.clone()); // avoid reference issues
    }

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique @Override public ItemStackSet getCachedParentTabStacks(@Nullable FeatureSet featureSet) {
        if (this.cachedParentTabStacks == null) {
            var player = MinecraftClient.getInstance().player;
            var currentFeatureSet = player != null ? player.networkHandler.getFeatureSet() : FeatureFlags.FEATURE_MANAGER.getFeatureSet();

            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(true); });
            ((ItemGroup)(Object)this).getDisplayStacks(featureSet != null ? featureSet : currentFeatureSet);
            Arrays.stream(ItemGroups.GROUPS).forEachOrdered((itemGroupX)->{ ((ItemGroupInterface) (Object) itemGroupX).setIgnoreInjection(false); });
        }
        return (this.cachedParentTabStacks = (ItemStackSet)this.cachedParentTabStacks.clone()); // avoid reference issues
    }

    // AVOID LOOPING! FASTER PERFORMANCE!
    @Unique @Override public ItemStackSet getCachedSearchTabStacks() { return this.getCachedSearchTabStacks(null); };
    @Unique @Override public ItemStackSet getCachedParentTabStacks() { return this.getCachedParentTabStacks(null); };

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
    }

    // clear cached
    @Inject(method = "clearStacks", at = @At("HEAD"))
    public void onClear(CallbackInfo ci) {
        this.cachedSearchTabStacks = null;
        this.cachedParentTabStacks = null;
    }

    //
    //@Inject(method = "getStacks", at = @At("RETURN"), cancellable=true)
    //public void getStackInject(FeatureSet enabledFeatures, boolean search, CallbackInfoReturnable<ItemStackSet> cir) {
        //final ItemStackSet setSorted = ItemGroupInterface.sortingAndTransfer(cir.getReturnValue(), (ItemGroup) (Object) this, enabledFeatures);

        //if (setSorted != null) {
            //cir.setReturnValue(setSorted);
            //cir.cancel();
        //}
    //}

    //
    @Redirect(method = "getStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;addItems(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;)V"))
    public void onItemAdd(ItemGroup instance, FeatureSet featureSet, ItemGroup.Entries entries) {
        instance.addItems(featureSet, entries);

        // interface
        var entriesInterface = ((ItemGroupEntriesInterface) entries);
        var entriesAccessor = (ItemGroupEntriesImplAccessor) entries;

        if (this.cachedSearchTabStacks == null) {
            this.cachedSearchTabStacks = (ItemStackSet) entriesAccessor.getSearchTabStacks().clone();
        }

        if (this.cachedParentTabStacks == null) {
            this.cachedParentTabStacks = (ItemStackSet) entriesAccessor.getParentTabStacks().clone();
        }

        entriesInterface.setParentTabStacks(entriesInterface.transferAndSorting(entriesAccessor.getParentTabStacks()));
        entriesInterface.setSearchTabStacks(entriesInterface.transferAndSorting(entriesAccessor.getSearchTabStacks()));
    }
}
