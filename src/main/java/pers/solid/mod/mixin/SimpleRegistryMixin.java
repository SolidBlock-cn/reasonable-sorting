package pers.solid.mod.mixin;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.MixinHelper;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin {


    @Shadow @Final private ObjectList<Item> rawIdToEntry;

    @Inject(method = "iterator",at=@At("HEAD"), cancellable = true)
    private void itemIterator(CallbackInfoReturnable<Iterator> cir) {
        var thisRegistry = (SimpleRegistry)(Object)this;
        if (thisRegistry == Registry.ITEM) {
            cir.setReturnValue(MixinHelper.itemRegistryIterator(this.rawIdToEntry));
        }
    }
}
