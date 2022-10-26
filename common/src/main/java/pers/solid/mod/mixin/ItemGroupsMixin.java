package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {

    /*
    @Shadow public static final ItemGroup BUILDING_BLOCKS = null;
    @Shadow public static final ItemGroup NATURE = null;
    @Shadow public static final ItemGroup REDSTONE = null;
    @Shadow public static final ItemGroup FUNCTIONAL = null;
    @Shadow public static final ItemGroup TOOLS = null;
    @Shadow public static final ItemGroup COMBAT = null;
    @Shadow public static final ItemGroup CONSUMABLES = null;
    @Shadow public static final ItemGroup CRAFTING = null;
    @Shadow public static final ItemGroup SPAWN_EGGS = null;
*/
}
