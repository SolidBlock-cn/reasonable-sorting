package pers.solid.mod.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.interfaces.ItemStackInterface;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackInterface {
    //
    @Shadow public abstract Item getItem();

    // Suggested alternative

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    @Inject(method = "isIn", at = @At("HEAD"), cancellable = true)
    public void isInMixin(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {

    }
}
