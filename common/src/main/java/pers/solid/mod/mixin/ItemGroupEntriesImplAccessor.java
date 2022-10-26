package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;

@Mixin(ItemGroup.EntriesImpl.class)
public interface ItemGroupEntriesImplAccessor extends ItemGroupEntriesInterface {
    // accessors
    @Accessor @Override public ItemStackSet getParentTabStacks();
    @Accessor @Override public ItemStackSet getSearchTabStacks();
    @Accessor @Override public FeatureSet getEnabledFeatures();
    @Accessor @Override public ItemGroup getGroup();
};
