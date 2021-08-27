package pers.solid.mod.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.MixinHelper;

import java.util.Iterator;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin {

    @Shadow
    @Final
    private ObjectList<Item> rawIdToEntry;

    @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
    private void itemIterator(CallbackInfoReturnable<Iterator> cir) {
        SimpleRegistry thisRegistry = (SimpleRegistry) (Object) this;
        if (thisRegistry == Registry.ITEM) {
            cir.setReturnValue(MixinHelper.itemRegistryIterator(this.rawIdToEntry));
        }
    }
}
