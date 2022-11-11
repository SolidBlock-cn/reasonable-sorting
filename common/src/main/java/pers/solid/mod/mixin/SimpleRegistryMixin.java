package pers.solid.mod.mixin;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.*;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends MutableRegistry<T> implements SimpleRegistryExtension {

  @Shadow
  @Final
  private ObjectList<T> rawIdToEntry;
  private @Nullable List<T> cachedEntries;

  public SimpleRegistryMixin(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
    super(registryKey, lifecycle);
  }

  @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
  private void reasonableSortedIterator(CallbackInfoReturnable<Iterator<T>> cir) {
    if (!Configs.instance.enableSorting || Configs.instance.sortingInfluenceRange != SortingInfluenceRange.REGISTRY) {
      return;
    }
    if (Configs.instance.sortingCalculationType != SortingCalculationType.STANDARD || cachedEntries == null) {
      final Stream<T> stream = SortingRule.streamOfRegistry(getKey(), rawIdToEntry);
      if (Configs.instance.sortingCalculationType == SortingCalculationType.STANDARD) {
        if (stream != null) {
          if (Configs.instance.debugMode) {
            SortingRule.LOGGER.info("Calculated cachedEntries for registry {}. You will not see this info for this registry again until you modify config.", getKey().getValue());
          }
          cachedEntries = stream.filter(Objects::nonNull).toList();
          cir.setReturnValue(cachedEntries.iterator());
        } else {
          cachedEntries = rawIdToEntry;
        }
      } else {
        if (stream != null) {
          if (Configs.instance.debugMode) {
            SortingRule.LOGGER.info("The iteration of registry {} is affected by Reasonable Sorting Mode, as the sorting calculation type is set to 'real-time' or 'semi-real-time'.", getKey().getValue());
          }
          cir.setReturnValue(stream.filter(Objects::nonNull).iterator());
        }
      }
    } else {
      cir.setReturnValue(Iterators.filter(cachedEntries.iterator(), Objects::nonNull));
    }
  }

  @Override
  public void removeCachedEntries() {
    cachedEntries = null;
  }
}
