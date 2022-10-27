package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import pers.solid.mod.interfaces.ItemGroupInterface;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor extends ItemGroupInterface {
    @Override @Accessor("displayStacks") public ItemStackSet getDisplayStacks();
    @Override @Accessor("searchTabStacks") public ItemStackSet getSearchTabStacks();

    @Override @Accessor("displayStacks") public void setDisplayStacks(ItemStackSet set);
    @Override @Accessor("searchTabStacks") public void setSearchTabStacks(ItemStackSet set);
}
