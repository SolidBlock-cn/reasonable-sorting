package pers.solid.mod.interfaces;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Unique;

public interface ItemGroupEntriesInterface {
    public default ItemStackSet getParentTabStacks() { return null; };
    public default ItemStackSet getSearchTabStacks() { return null; };
    public default FeatureSet getEnabledFeatures() { return null; };
    public default ItemGroup getGroup() { return null; };
};
