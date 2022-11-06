package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.util.Pair;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ItemGroup.EntriesImpl.class)
public class ItemGroupEntriesImplMixin implements ItemGroupEntriesInterface {

    @Unique @Override
    public List<Pair<ItemGroup, ItemStack>> sorting(List<Pair<ItemGroup, ItemStack>> itemStackSet, boolean hasPermission) {
        var group = ((ItemGroupEntriesImplAccessor)this).getGroup();
        var enabledFeatures = ((ItemGroupEntriesImplAccessor)this).getEnabledFeatures();
        return ItemGroupInterface.sorting(itemStackSet, enabledFeatures, hasPermission);
    }

}
