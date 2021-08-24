package pers.solid.mod.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroup.class)
public class ItemGroupMixin {
    @Inject(method = "appendStacks", at = @At("HEAD"))
    private void appendStacksMixin(DefaultedList<ItemStack> stacks, CallbackInfo ci) {
//        MixinHelper.APPENDED_ITEMS.clear();
    }
}
