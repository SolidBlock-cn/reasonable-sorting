package pers.solid.mod.mixin;

import com.google.common.collect.Collections2;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.*;

import java.util.*;
import java.util.stream.Stream;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends MutableRegistry<T> implements SimpleRegistryExtension {
  @Shadow
  @Final
  private Map<T, RegistryEntry.Reference<T>> valueToEntry;

  @Shadow
  protected abstract List<RegistryEntry.Reference<T>> getEntries();

  @Shadow
  @Nullable
  private List<RegistryEntry.Reference<T>> cachedEntries;

  @Shadow
  public abstract Optional<RegistryEntry<T>> getEntry(int rawId);

  @Shadow
  public abstract RegistryEntry<T> replace(OptionalInt rawId, RegistryKey<T> key, T newEntry, Lifecycle lifecycle);

  @Shadow
  public abstract Optional<RegistryKey<T>> getKey(T entry);

  public SimpleRegistryMixin(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
    super(registryKey, lifecycle);
  }

  @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
  private void reasonableSortedIterator(CallbackInfoReturnable<Iterator<T>> cir) {
    if (!Configs.instance.enableSorting || Configs.instance.sortingInfluenceRange != SortingInfluenceRange.REGISTRY) {
      return;
    }
    if (Configs.instance.sortingCalculationType == SortingCalculationType.REAL_TIME || Configs.instance.sortingCalculationType == SortingCalculationType.SEMI_REAL_TIME) {
      final Collection<SortingRule<T>> sortingRules = SortingRule.getSortingRules(getKey());
      if (sortingRules.isEmpty()) return;

      final Stream<T> stream = SortingRule.streamOfRegistry(getKey(), Collections2.transform(getEntries(), RegistryEntry.Reference::value), sortingRules);
      if (Configs.instance.debugMode) {
        SortingRule.LOGGER.info("The iteration of registry {} is affected by Reasonable Sorting Mode, as the sorting calculation type is set to 'real-time' or 'semi-real-time'.", getKey().getValue());
      }
      cir.setReturnValue(stream.iterator());
      cir.cancel();
    }
  }

  @Inject(method = "getEntries", at = @At(value = "FIELD", target = "Lnet/minecraft/util/registry/SimpleRegistry;cachedEntries:Ljava/util/List;", ordinal = 1, shift = At.Shift.AFTER))
  private void reasonableSortedCachedEntries(CallbackInfoReturnable<List<RegistryEntry.Reference<T>>> cir) {
    if (!Configs.instance.enableSorting || Configs.instance.sortingInfluenceRange != SortingInfluenceRange.REGISTRY) {
      return;
    }
    // 此处只会在 cachedEntries 不为 null 时执行。
    if (Configs.instance.sortingCalculationType == SortingCalculationType.STANDARD) {

      final Collection<SortingRule<T>> sortingRules = SortingRule.getSortingRules(getKey());
      if (sortingRules.isEmpty()) return;
      final Stream<T> stream = SortingRule.streamOfRegistry(getKey(), Collections2.transform(getEntries(), RegistryEntry.Reference::value), sortingRules);
      if (Configs.instance.debugMode) {
        SortingRule.LOGGER.info("Calculated cachedEntries for registry {}. You will not see this info for this registry again until you modify config.", getKey().getValue());
      }
      cachedEntries = stream.map(value -> this.valueToEntry.get(value)).toList();
    }
  }

  @Override
  public void removeCachedEntries() {
    cachedEntries = null;
  }
}
