# Reasonable Sorting

如果看不懂英文，可以阅读[中文版](README.md)。

Did you find that, in Creative Mode, it's quite a hard work to find in the creative inventory anything you want, as the sorting of them is so messy?

This mod adjusts the order that items are iterated in the registry, to adjust the sorting of items. As it's the change on the layer of item registry, it works fine when you view the item list through other mods (like RoughlyEnoughItems).

Besides, this mod allows you to change the item groups to which items belong.

This mod is base on Fabric and **depends on Fabric API and Cloth Config mod**, without which this mod cannot work. Besides, **it's recommended to install Mod Menu** to configure. (If you have installed these mod please do not install duplicate ones. Moreover, some mods, like Bedrockify, Edit Sign, Better F3 may include nested Cloth Config mod. In these cases you do not need to install separately Cloth Config).

## Configuration

You can configure this mod through Mod Menu.

### Sorting

**Enable sorting**

ON by default. If OFF, all the sorting works as vanilla, and following configurations will not work. The sorting mainly takes effect on your creative inventory and item lists in mods like Roughly Enough Item (on which this mod does not rely).

**Enable default item sorting rules**

ON by default. This mod has some installed item sorting rules. For instance, Ice, Packed Ice and Blue Ice are sorted together.

**Custom sorting rule**

Empty by default. You can have custom sorting rules by typing item id. In the mod config screen, click the "+" on the left, and you will see an unapparent text box on the below. Type one rule in it. The syntax of a rule is: multiple item ids are separated with a space. For example, `dirt white_wool diamond_block` means that Dirt, White Wool and Diamond Block become together, in which White Wools and Diamond Block follow Dirt.

**Variants following base blocks**

In Minecraft, many blocks have their **variants**. For example, of Oak Planks, the "stairs" variant is "Oak Stairs" and "slab" variant is "Oak Slab", which also means, Oak Planks is the **base block** or Oak Stairs, Oak Slab etc. You can specify some variants which will follow their base blocks.

The default syntax is, multiple block variant names are separated with a space. Available variant names are displayed in the mod config screen. By default `stairs slab`, which means, all stairs and slabs follow their base blocks.

Take notice that changing item sorting does not affect which item group they belong to. To change item groups you need also to config **Variant transfer rules**.

**Fence gate follows fence**

ON by default. Makes all fence gate blocks follow their corresponding fence blocks. Requires **Fence gates in decorations** otherwise their still appear in "Redstone" item group and does not take effect.

### Item group transfer

**Enable item group transfer**

ON by default. If OFF, all items appear in the item groups they belong to in vanilla, and following configs will not work.

**Buttons in decorations**

**Fence gates in decorations**

**Swords in tools**

**Doors in decorations**

The meaning of the four entries above is obvious. By default, "Fence gates in decorations" is ON, and others are OFF.

**Custom item group transfer rules**

Empty by default. Similar to custom sorting rules, one rule every line, and add a new rule by clicking "+". The syntax of each rule is: item id, space, the item group to transfer to. For example, `redstone_block building_blocks` will transfer Redstone Block to "Building Blocks" item group.

**Custom variant transfer rules**

Empty by default. Similar to above. Syntax: variant name, space, and the item group to transfer to. For example,`cut transportation` transfers all cut blocks to "Transportations".

**Custom regex transfer rules**

Empty by default. Similar to above. Syntax: regex, space, and the item group to transfer to. For example, `.+?button transportation` transfers to all items which identifier ends with `button` to "Transportations".

## To develop

This mod recognizes item sorting rules and transfer rules in other mods. You do not need to use codes in this mod, nor to modify `build.gradle`, ~~as I do not know about it~~.

To make this mod use your own item sorting rule, implement `Supplier<Collection<Map<Item, Collection<Item>>>>` in your class, override `get` method, and add it to `reasonable-sorting:item_sorting_rules` entrypoint in `fabric.mod.json`. You can also add directly your static methods to the entrypoint.

This method returns a collection, of which elements are mappings from item to collection of items. Take notice that this collection is copied into inner variables of this mod when initializing this mod, so adding or removing elements of this collection after initialization will not work. But elements in this collection are directly passed to this mod with the same pointer, instead of being copied.

Therefore, unexpected behaviours might be found if you want to access mod config to determine whether to contain certain elements in the collection you return, as changing configs after initializing does not work. Yet you can, when loading or updating configs, call clear or add to elements themselves instead of changing their pointers, or directly use `ForwardingMap` as elements of the collection, and decide its `delegate` through configs.

To make this mod use your own item transfer rule, implement `Supplier<Collection<Map<Item, ItemGroup>>>` and override `get` method, then add the class to `reasonable-sorting:item_group_transfer_rules` entrypoint. Adding directly a static method to the entry point is also ok.

Were you still confused, you may refer to codes of this mod.