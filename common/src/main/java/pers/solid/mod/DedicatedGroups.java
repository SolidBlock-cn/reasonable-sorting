package pers.solid.mod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public abstract class DedicatedGroups {
    public static ArrayList<Item> SYSTEM_ITEMS_LIST = null;
    public static ArrayList<ItemGroup> ITEM_GROUPS = null;
    public static ItemGroup SYSTEM_ITEMS = null;

    public static ItemGroup getSystemGroup() {
        if (SYSTEM_ITEMS_LIST == null) {
            //
            var list = (SYSTEM_ITEMS_LIST = new ArrayList<>());
            {
                list.add(Items.COMMAND_BLOCK);
                list.add(Items.CHAIN_COMMAND_BLOCK);
                list.add(Items.REPEATING_COMMAND_BLOCK);
                list.add(Items.COMMAND_BLOCK_MINECART);
                list.add(Items.LIGHT);
                list.add(Items.STRUCTURE_BLOCK);
                list.add(Items.STRUCTURE_VOID);
                list.add(Items.JIGSAW);
                list.add(Items.DEBUG_STICK);
                list.add(Items.BARRIER);
                list.add(Items.SPAWNER);
                list.add(Items.DRAGON_EGG);
            }
        }

        // I don't prefer put into exist tabs
        if (SYSTEM_ITEMS == null) {
            SYSTEM_ITEMS = new FabricItemGroup(new Identifier("dawn_api", "system")) {
                @Override public ItemStack createIcon() {return new ItemStack(Items.COMMAND_BLOCK); }
                @Override public void addItems(FeatureSet enabledFeatures, ItemGroup.Entries list, boolean hasPermission) {
                    if (hasPermission && Configs.instance.transferSystemItems) {
                        SYSTEM_ITEMS_LIST.forEach((I) -> list.add((Item) I));
                    }
                }
            };
        }

        //
        return SYSTEM_ITEMS;
    };

    public static ArrayList<ItemGroup> getGroups() {
        if (ITEM_GROUPS == null) {
            getSystemGroup(); ITEM_GROUPS = new ArrayList(List.of(ItemGroups.GROUPS));
        };
        return ITEM_GROUPS;
    }

}
