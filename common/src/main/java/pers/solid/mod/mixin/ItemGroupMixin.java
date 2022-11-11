package pers.solid.mod.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pers.solid.mod.SortingRule;

import java.util.Iterator;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {
  @Shadow
  public abstract String getName();

  @ModifyVariable(method = "appendStacks", at = @At("STORE"))
  public Iterator<Item> modifiedAppendStacks(Iterator<Item> value) {
    return SortingRule.modifyIteratorInInventory(value, getName());
  }
}
