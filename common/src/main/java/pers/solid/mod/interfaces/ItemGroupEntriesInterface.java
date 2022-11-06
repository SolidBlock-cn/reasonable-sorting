package pers.solid.mod.interfaces;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

public interface ItemGroupEntriesInterface {
    public default ItemStackSet getParentTabStacks() { return null; };
    public default ItemStackSet getSearchTabStacks() { return null; };
    public default FeatureSet getEnabledFeatures() { return null; };
    public default ItemGroup getGroup() { return null; };

    public default void setParentTabStacks(ItemStackSet set) {};
    public default void setSearchTabStacks(ItemStackSet set) {};

    //
    public default List<Pair<ItemGroup, ItemStack>>  transfer(List<Pair<ItemGroup, ItemStack>> itemStackSet, boolean hasPermission) { return itemStackSet; };
    public default List<Pair<ItemGroup, ItemStack>>  sorting(List<Pair<ItemGroup, ItemStack>>  itemStackSet, boolean hasPermission) { return itemStackSet; };
};
