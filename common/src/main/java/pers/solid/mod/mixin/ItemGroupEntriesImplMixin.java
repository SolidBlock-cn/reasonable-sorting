package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;
import pers.solid.mod.interfaces.ItemGroupEntriesInterface;
import pers.solid.mod.interfaces.ItemGroupInterface;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ItemGroup.EntriesImpl.class)
public class ItemGroupEntriesImplMixin implements ItemGroupEntriesInterface {

    @Unique @Override
    public ItemStackSet sorting(ItemStackSet itemStackSet, boolean hasPermission) {
        var group = ((ItemGroupEntriesImplAccessor)this).getGroup();
        var enabledFeatures = ((ItemGroupEntriesImplAccessor)this).getEnabledFeatures();
        return ItemGroupInterface.sorting(itemStackSet, group, enabledFeatures, hasPermission);
    }

    @Unique @Override
    public ItemStackSet exclude(ItemStackSet itemStackSet, boolean hasPermission) {
        var group = ((ItemGroupEntriesImplAccessor)this).getGroup();
        var enabledFeatures = ((ItemGroupEntriesImplAccessor)this).getEnabledFeatures();
        return ItemGroupInterface.exclude(itemStackSet, group, enabledFeatures, hasPermission);
    }

}
