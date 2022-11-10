package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;
import pers.solid.mod.interfaces.ItemGroupInterface;

import java.util.List;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {

    @Shadow
    public static List<ItemGroup> getGroups() {
        return null;
    }

    //
    @Inject(method = "updateEntries", at = @At(value = "RETURN"))
    private static void unUpdateEntries(FeatureSet enabledFeatures, boolean operatorEnabled, CallbackInfo ci) {

        // make a cache
        getGroups().stream().forEach((instance) -> {
            //
            var originalParentStacksRef = (ItemStackSet)((ItemGroupInterface) instance).getCachedParentTabStacks(false, operatorEnabled);
            var originalSearchStacksRef = (ItemStackSet)((ItemGroupInterface) instance).getCachedSearchTabStacks(false, operatorEnabled);

            //
            if (originalParentStacksRef == null) { originalParentStacksRef = ((ItemGroupInterface) instance).setCachedParentTabStacks((ItemStackSet)instance.getDisplayStacks().clone()); };
            if (originalSearchStacksRef == null) { originalSearchStacksRef = ((ItemGroupInterface) instance).setCachedSearchTabStacks((ItemStackSet)instance.getSearchTabStacks().clone()); };
        });

        // sorting and transfer by ready data
        getGroups().stream().forEach((instance) -> {
            //
            var originalParentStacksRef = (ItemStackSet)((ItemGroupInterface) instance).getCachedParentTabStacks(false, operatorEnabled);
            var originalSearchStacksRef = (ItemStackSet)((ItemGroupInterface) instance).getCachedSearchTabStacks(false, operatorEnabled);

            // reference, from empty
            var emptyParentStacksRef = (ItemStackSet)(originalParentStacksRef).clone(); emptyParentStacksRef.clear();
            var emptySearchStacksRef = (ItemStackSet)(originalSearchStacksRef).clone(); emptySearchStacksRef.clear();

            //
            if (!(instance == ItemGroups.INVENTORY || instance == ItemGroups.SEARCH || instance == ItemGroups.HOTBAR)) {
                // transfer and sorting
                ItemGroupInterface.transfer((ItemStackSet) emptyParentStacksRef, (ItemStackSet) emptySearchStacksRef, instance, enabledFeatures, operatorEnabled);

                //
                ((ItemGroupInterface) instance).setDisplayStacks((ItemStackSet) emptyParentStacksRef);
                ((ItemGroupInterface) instance).setSearchTabStacks((ItemStackSet) emptySearchStacksRef);
            }
        });
    }

}
