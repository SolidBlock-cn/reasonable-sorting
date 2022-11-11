package pers.solid.mod.forge.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingInfluenceRange;
import pers.solid.mod.SortingRule;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * 这个是专用于 {@link ForgeRegistry} 的 mixin。由于 Forge 的注册表替代了原版的注册表，故需要为 Forge 单独写一个 mixin。
 */
@Mixin(ForgeRegistry.class)
public abstract class ForgeRegistryMixin<V> {
  @Shadow
  public abstract RegistryKey<Registry<V>> getRegistryKey();

  @Inject(method = "iterator", at = @At("RETURN"), remap = false, cancellable = true)
  private void reasonableSortedIterator(CallbackInfoReturnable<Iterator<V>> cir) {
    if (!Configs.instance.enableSorting || Configs.instance.sortingInfluenceRange != SortingInfluenceRange.REGISTRY) {
      return;
    }
    final Stream<V> stream = SortingRule.streamOfRegistry(getRegistryKey(), ImmutableList.copyOf(cir.getReturnValue()));
    if (stream != null) {
      cir.setReturnValue(stream.iterator());
      cir.cancel();
    }
  }
}
