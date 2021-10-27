package pers.solid.mod.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.collection.Int2ObjectBiMap;
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
import pers.solid.mod.Configs;
import pers.solid.mod.MixinHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends MutableRegistry<T> {

    @Shadow
    @Final
    protected Int2ObjectBiMap<T> indexedEntries;

    public SimpleRegistryMixin(RegistryKey<Registry<T>> registryKey, Lifecycle lifecycle) {
        super(registryKey, lifecycle);
    }


    @SuppressWarnings({"RedundantCast"})
    @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
    private void itemIterator(CallbackInfoReturnable<Iterator<T>> cir) {
        if (Configs.CONFIG_HOLDER.getConfig().enableSorting && this.equals(Registry.ITEM)) {
            try {
                cir.setReturnValue(MixinHelper.itemRegistryIterator(this.indexedEntries, (Collection<? extends Map<T, ? extends Collection<T>>>) (Collection) MixinHelper.ITEM_SORTING_RULES));
            } catch (ClassCastException ignored) {
            }
        }
    }
}
