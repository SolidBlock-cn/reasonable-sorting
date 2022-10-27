package pers.solid.mod.interfaces;

import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;

public interface ItemGroupInterface {

    public void setIgnoreInjection(boolean ignoreInjection);
    public default ItemStackSet getCachedSearchTabStacks() { return null; };
    public default ItemStackSet getCachedParentTabStacks() { return null; };
    public default ItemStackSet getCachedSearchTabStacks(FeatureSet featureSet) { return null; };
    public default ItemStackSet getCachedParentTabStacks(FeatureSet featureSet) { return null; };
}
