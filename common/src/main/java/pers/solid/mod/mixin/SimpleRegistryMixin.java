package pers.solid.mod.mixin;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
import pers.solid.mod.SortingRule;

import java.util.Iterator;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends MutableRegistry<T> {

  @Shadow
  @Final
  private ObjectList<T> rawIdToEntry;

  public SimpleRegistryMixin(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
    super(registryKey, lifecycle);
  }

  @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
  private void itemIterator(CallbackInfoReturnable<Iterator<T>> cir) {
    final Iterator<T> iterator = SortingRule.iteratorOfRegistry(getKey(), rawIdToEntry);
    if (iterator != null) {
      cir.setReturnValue(iterator);
      cir.cancel();
    }
  }
}
