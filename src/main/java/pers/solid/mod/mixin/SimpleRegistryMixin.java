package pers.solid.mod.mixin;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.registry.*;
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
  private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

  public SimpleRegistryMixin(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
    super(registryKey, lifecycle);
  }

  @SuppressWarnings({"RedundantCast"})
  @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
  private void itemIterator(CallbackInfoReturnable<Iterator<T>> cir) {
    if (Configs.CONFIG_HOLDER.getConfig().enableSorting && this.equals(Registry.ITEM)) {
      try {
        cir.setReturnValue(
            MixinHelper.itemRegistryIterator(
                (this.rawIdToEntry),
                (Collection<? extends Map<T, ? extends Collection<T>>>)
                    (Collection) MixinHelper.ITEM_SORTING_RULES));
      } catch (ClassCastException ignored) {
      }
    }
  }
}
