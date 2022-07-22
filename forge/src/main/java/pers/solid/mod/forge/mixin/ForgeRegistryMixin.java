package pers.solid.mod.forge.mixin;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.SortingRule;

import java.util.*;
import java.util.stream.Stream;

/**
 * 这个是专用于 {@link ForgeRegistry} 的 mixin。由于 Forge 的注册表替代了原版的注册表，故需要为 Forge 单独写一个 mixin。
 */
@Mixin(ForgeRegistry.class)
public abstract class ForgeRegistryMixin<V> {
  @Shadow
  public abstract RegistryKey<Registry<V>> getRegistryKey();

  @Shadow
  public abstract Identifier getRegistryName();

  @Inject(method = "iterator", at = @At("RETURN"), remap = false, cancellable = true)
  private void reasonableSortedIterator(CallbackInfoReturnable<Iterator<V>> cir) {
    final RegistryKey<Registry<V>> registryKey = getRegistryKey();
    final Collection<SortingRule<V>> ruleSets = SortingRule.getSortingRules(registryKey);
    if (ruleSets.isEmpty()) return;
    SortingRule.LOGGER.info("{} sorting rules found in the iteration of {}.", ruleSets.size(), getRegistryName().toString());
    LinkedHashSet<V> iterated = new LinkedHashSet<>();
    Iterator<V> iterator = cir.getReturnValue();
    final List<V> copy = Lists.newArrayList(iterator);
    // 被确认跟随在另一对象之后，不因直接在一级迭代产生，而应在一级迭代产生其他对象时产生的对象。
    // 一级迭代时，就应该忽略这些对象。
    // 本集合仅用于检测对象是否存在，故不考虑顺序。
    final Set<V> combinationFollowers = new HashSet<>();

    // 本集合的键为被跟随的对象，值为跟随者它的对象。
    final Multimap<V, V> valueToFollowers = LinkedListMultimap.create();

    // 初次直接迭代内部元素。
    for (V entry : copy) {
      SortingRule.streamFollowersOf(ruleSets, entry).forEach(follower -> {
        valueToFollowers.put(entry, follower);
        combinationFollowers.add(follower);
      });
    }

    // 结果流的第一部分。先将内容连同其跟随者都迭代一次，已经迭代过的不重复迭代。但是，这部分可能会丢下一些元素。
    final Stream<V> firstStream = copy.stream()
        .filter(o -> !combinationFollowers.contains(o))
        .flatMap(o -> SortingRule.oneAndItsFollowers(o, valueToFollowers))
        .filter(o -> !iterated.contains(o))
        .peek(iterated::add);

    // 第一次未迭代完成的，在第二次迭代。
    final Stream<V> secondStream = copy.stream()
        .filter((x -> !iterated.contains(x)))
        .peek(o -> SortingRule.LOGGER.info("Object {} not iterated in the first iteration. Iterated in the second iteration.", o));

    cir.setReturnValue(Stream.concat(firstStream, secondStream).iterator());
  }
}
