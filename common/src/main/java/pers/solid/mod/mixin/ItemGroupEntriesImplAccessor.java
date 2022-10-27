package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;

@Mixin(ItemGroup.EntriesImpl.class)
public interface ItemGroupEntriesImplAccessor extends ItemGroupEntriesInterface {
    // accessors
    @Accessor("parentTabStacks") @Override public ItemStackSet getParentTabStacks();
    @Accessor("searchTabStacks") @Override public ItemStackSet getSearchTabStacks();

    @Accessor("parentTabStacks") @Override public void setParentTabStacks(ItemStackSet set);
    @Accessor("searchTabStacks") @Override public void setSearchTabStacks(ItemStackSet set);

    @Accessor("enabledFeatures") @Override public FeatureSet getEnabledFeatures();
    @Accessor("group") @Override public ItemGroup getGroup();
};
