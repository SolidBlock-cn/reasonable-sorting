package pers.solid.mod.interfaces;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface ItemGroupEntriesInterface {
    public default ItemStackSet getParentTabStacks() { return null; };
    public default ItemStackSet getSearchTabStacks() { return null; };
    public default FeatureSet getEnabledFeatures() { return null; };
    public default ItemGroup getGroup() { return null; };

    public default void setParentTabStacks(ItemStackSet set) {};
    public default void setSearchTabStacks(ItemStackSet set) {};

    //
    public default ItemStackSet exclude(ItemStackSet itemStackSet) { return (ItemStackSet)itemStackSet.clone(); };
    public default ItemStackSet transfer(ItemStackSet itemStackSet) { return (ItemStackSet)itemStackSet.clone(); };
    public default ItemStackSet sorting(ItemStackSet itemStackSet) { return (ItemStackSet)itemStackSet.clone(); };
};
