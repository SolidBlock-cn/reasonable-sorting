package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;

@Mixin(ItemGroup.Entries.class)
public interface ItemGroupEntriesMixin extends ItemGroupEntriesInterface {
    @Override @Unique public default ItemStackSet getParentTabStacks() { return null; };
    @Override @Unique public default ItemStackSet getSearchTabStacks() { return null; };
    @Override @Unique public default FeatureSet getEnabledFeatures() { return null; };
    @Override @Unique public default ItemGroup getGroup() { return null; };
};
