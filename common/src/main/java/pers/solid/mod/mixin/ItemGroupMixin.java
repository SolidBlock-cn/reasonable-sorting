package pers.solid.mod.mixin;

import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {
    @Shadow public abstract void addItems(FeatureSet enabledFeatures, ItemGroup.Entries entries);

    @Shadow public abstract ItemStackSet getSearchTabStacks(FeatureSet enabledFeatures);

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void isInMixin(FeatureSet enabledFeatures, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ItemGroup group = (ItemGroup)(Object)this;
        if (group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
        final Item item = stack.getItem();
        final Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(item).collect(Collectors.toSet());
        if (!groups.isEmpty()) {
            cir.setReturnValue(groups.contains(group));
            cir.cancel();
        }
    }

    @Inject(method = "getStacks", at = @At("RETURN"), cancellable=true)
    public void getStackInject(FeatureSet enabledFeatures, boolean search, CallbackInfoReturnable<ItemStackSet> cir) {
        var itemStackSet = SortingRule.sortItemGroupEntries(cir.getReturnValue());
        if (itemStackSet != null) {
            System.err.println("Should to be changed!");
            cir.setReturnValue(itemStackSet);
            cir.cancel();
        }
    }

    // TODO! Needs correct sorting
    @Redirect(method = "getStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;addItems(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;)V"))
    public void resort(ItemGroup instance, FeatureSet featureSet, ItemGroup.Entries entries) {
        ((ItemGroup) (Object)instance).addItems(featureSet, entries);
    }
}
