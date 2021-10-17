package pers.solid.mod.mixin;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.item.Item;
import net.minecraft.util.registry.MutableRegistry;
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

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends MutableRegistry<T> {

    @Shadow
    @Final
    private ObjectList<Item> rawIdToEntry;

    public SimpleRegistryMixin(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
        super(registryKey, lifecycle);
    }

    @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
    private void itemIterator(CallbackInfoReturnable<Iterator<T>> cir) {
        if (this.equals(Registry.ITEM)) {
            cir.setReturnValue((Iterator<T>) MixinHelper.itemRegistryIterator(this.rawIdToEntry,MixinHelper.ITEM_COMBINATION_RULES));
        }
    }
}
