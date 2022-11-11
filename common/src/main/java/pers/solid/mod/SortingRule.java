package pers.solid.mod;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * <p>一个排序规则。这种规则的实质是，决定一个对象之后应该由哪些对象跟随。
 * <p>A sorting rule, which is in essence, determining what object or objects should follow a specific object.
 * <p>例如，这个规则可以让石化橡木台阶跟随在橡木台阶后面：
 * <p>For example, the following rule makes a petrified oak slab follows an oak slab:
 * <pre>{@code
 * SortingRule<Block> myRule = block -> block == Blocks.OAK_SLAB ? Collections.singleton(Blocks.PETRIFIED_OAK_SLAB) : null}
 * </pre>
 * <p>这个规则会让橡木台阶返回一个紧随石化橡木台阶的单元素集，也就是说橡木台阶后面紧随着石化橡木台阶。对于其他方块则返回 null，也就是不受影响。
 * <p>The rule makes oak slab returns a singleton set of a petrified oak slab, which means an oak slab should be followed by a petrified oak slab. For other blocks, it returns null, which means no influence.
 *
 * @param <T>
 */
@FunctionalInterface
public interface SortingRule<T> {

  @ApiStatus.Internal
  Logger LOGGER = LogManager.getLogger(SortingRule.class);

  @SuppressWarnings("unchecked")
  static <T> Multimap<T, T> getValueToFollowersCache(RegistryKey<? extends Registry<T>> registryKey) {
    return (Multimap<T, T>) Internal.VALUE_TO_FOLLOWER_CACHES.get(registryKey);
  }

  static <T> void setValueToFollowersCache(RegistryKey<? extends Registry<T>> registryKey, Multimap<T, T> valueToFollowersCache) {
    Internal.VALUE_TO_FOLLOWER_CACHES.put(registryKey, valueToFollowersCache);
  }

  static <T> Multimap<T, T> getOrCreateValueToFollowers(RegistryKey<? extends Registry<T>> registryKey, Collection<T> entries) {
    Multimap<T, T> valueToFollowersCache = getValueToFollowersCache(registryKey);
    if (valueToFollowersCache == null || Configs.instance.sortingCalculationType == SortingCalculationType.REAL_TIME) {
      if (Configs.instance.debugMode) {
        LOGGER.info("The value-to-followers cache does not exist in registry key {}. It may happen when you start game or open your inventory at first. Creating a new one fo this registry.", registryKey.getValue());
      }
      valueToFollowersCache = LinkedListMultimap.create();
      setValueToFollowersCache(registryKey, valueToFollowersCache);
      Multimap<T, T> finalValueToFollowersCache = valueToFollowersCache;
      entries.forEach(value -> finalValueToFollowersCache.putAll(value, SortingRule.streamFollowersOf(SortingRule.getSortingRules(registryKey), value).toList()));
      if (Configs.instance.debugMode && !valueToFollowersCache.isEmpty()) {
        final T exampleKey = valueToFollowersCache.keys().iterator().next();
        LOGGER.info("Built value-to-followers cache for registry {} with {} elements, such as {} -> {}.", registryKey.getValue(), valueToFollowersCache.size(), exampleKey, valueToFollowersCache.get(exampleKey));
      }
    }
    return valueToFollowersCache;
  }

  static void clearValueToFollowersCache() {
    if (Configs.instance.debugMode) {
      LOGGER.info("Clearing caches of sorting rules of {} registries.", Internal.VALUE_TO_FOLLOWER_CACHES.size());
    }
    Internal.VALUE_TO_FOLLOWER_CACHES.clear();
  }


  /**
   * 添加一个规则。迭代注册表时就会应用到此规则。
   *
   * @param registryKey 该注册表的注册表键。当迭代注册表时，如果注册表的键符合，那么就会使用这个规则。
   * @param rule        一个排序规则。它接收一个对象，并返回这个对象应该被哪些对象跟随。如果为 {@code null}，那么表示这个对象没有被其他对象跟随。
   * @param <T>         对象的类型。
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T> void addSortingRule(RegistryKey<? extends Registry<T>> registryKey, SortingRule<T> rule) {
    ((Multimap<RegistryKey<? extends Registry<T>>, SortingRule<T>>) (Multimap) Internal.RULES).put(registryKey, rule);
    SimpleRegistryExtension.removeAllCachedEntries();
  }

  /**
   * 添加一个有条件的规则。只有当条件符合时，该规则才会被应用。
   *
   * @param registryKey 该注册表的注册表键。当迭代时，如果注册表的键符合，那么就会使用这个规则。
   * @param condition   应用该排序规则的一个条件。
   * @param rule        一个排序规则。
   * @param <T>         对象的类型
   */
  static <T> void addConditionalSortingRule(RegistryKey<? extends Registry<T>> registryKey, BooleanSupplier condition, SortingRule<T> rule) {
    addSortingRule(registryKey, leadingObj -> condition.getAsBoolean() ? rule.getFollowers(leadingObj) : null);
  }

  /**
   * 根据一个注册表键，返回已经注册了的规则的集合。
   *
   * @param registryKey 注册表键。
   * @param <T>         对象的类型。
   * @return 规则的集合。
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T> Collection<SortingRule<T>> getSortingRules(RegistryKey<? extends Registry<T>> registryKey) {
    return ((Multimap<RegistryKey<? extends Registry<T>>, SortingRule<T>>) (Multimap) Internal.RULES).get(registryKey);
  }

  /**
   * 根据规则的集合以及可能适用该规则的对象，返回该对象的跟随者的流。若集合中有多个规则，则规则产生的跟随者集合会自动合并。注意该流可能产生重复元素。
   *
   * @param combinationRules 排序规则集合，一般由 {@link #getSortingRules(RegistryKey)} 返回。
   * @param object           可能适用规则并需要查找跟随者的对象。
   * @param <T>              对象的类型。
   * @return 对象的跟随者的流，可能是空的。
   */
  static <T> Stream<T> streamFollowersOf(Collection<SortingRule<T>> combinationRules, T object) {
    return combinationRules.stream().map(function -> function.getFollowers(object)).filter(Objects::nonNull).flatMap(Streams::stream);
  }

  /**
   * 替换一个普通注册表的流，以通过其迭代器应用排序规则。
   *
   * @see SimpleRegistry#iterator()
   * @see SimpleRegistry#stream()
   * @see pers.solid.mod.mixin.SimpleRegistryMixin
   */
  static <T> @Nullable Stream<T> streamOfRegistry(
      RegistryKey<? extends Registry<T>> registryKey,
      Collection<T> entries) {
    LinkedHashSet<T> iterated = new LinkedHashSet<>();
    final Collection<SortingRule<T>> ruleSets = getSortingRules(registryKey);

    if (ruleSets.isEmpty()) {
      // 如果没有为此注册表设置规则，那么直接返回 null，在 mixin 中表示依然按照原版的迭代方式迭代。
      return null;
    } else {
      LOGGER.info("{} sorting rules found in the iteration of {}.", ruleSets.size(), registryKey.getValue());
    }

    // 被确认跟随在另一对象之后，不因直接在一级迭代产生，而应在一级迭代产生其他对象时产生的对象。
    // 一级迭代时，就应该忽略这些对象。

    // 本集合的键为被跟随的对象，值为跟随者它的对象。
    final Multimap<T, T> valueToFollowers = getOrCreateValueToFollowers(registryKey, entries);

    // 结果流的第一部分。先将内容连同其跟随者都迭代一次，已经迭代过的不重复迭代。但是，这部分可能会丢下一些元素。
    final Stream<T> firstStream = entries.stream()
        .filter(o -> !valueToFollowers.containsValue(o))
        .flatMap(o -> oneAndItsFollowers(o, valueToFollowers))
        .filter(o -> !iterated.contains(o))
        .peek(iterated::add);

    // 第一次未迭代完成的，在第二次迭代。
    final Stream<T> secondStream = entries.stream()
        .filter((x -> !iterated.contains(x)))
        .peek(o -> LOGGER.info("Object {} not iterated in the first iteration or {}. Iterated in the second iteration.", o, registryKey.getValue()));

    return Stream.concat(firstStream, secondStream);
  }

  /**
   * 根据一个对象，创建一个它自己及其跟随者的流。跟随者的跟随者也会包含在这里面。
   *
   * @return 对象自身及其跟随者组成的流。
   */
  static <T> Stream<T> oneAndItsFollowers(T o, Multimap<T, T> valueToFollowers) {
    final Stream<T> followersStream = valueToFollowers.get(o).stream();
    return Stream.concat(Stream.of(o), followersStream.flatMap(o1 -> oneAndItsFollowers(o1, valueToFollowers)));
  }

  /**
   * <p>根据一个 leadingObj 对象，决定它应该被哪些对象跟随。可以返回 null 或空集（表示不影响），或返回由其他对象组成的集合（注意集合不能包含 leadingObj 本身）。
   * <p>With regard to a leadingObj, determine what object or objects should follow it. It may return null or an empty set (indicating no influence), or return a set of other objects (the set cannot contain the leadingObj itself).
   *
   * @param leadingObj 被紧随的对象。
   * @return 跟随该对象的对象集合。
   */
  @Nullable Iterable<T> getFollowers(T leadingObj);

  @ApiStatus.Internal
  class Internal {

    /**
     * <p>当前已有的排序规则的集。每个注册表键都可以指定排序规则。一个规则可以指定某个对象被接下来的哪些对象跟随，也就是说这几个对象“紧挨在一起”。这个函数应该接收一个对象并返回跟随者的集合（可以为 {@code null}），以让这个对象与它的跟随者“紧挨一起”。
     * <p>一个简单的例子是：对于方块注册表，应用类似如下的规则：</p>
     * <blockquote>橡木木板 -> List.of(橡木楼梯, 橡木台阶)<br>
     * 白桦木板 -> List.of(白桦木楼梯, 白桦木台阶)<br>
     * ……</blockquote>
     * <p>那么，迭代时，橡木楼梯就会跟随在橡木台阶的后面，以此类推。
     * <p>当然，每个注册表可以指定多个规则，这多个规则集就会合并。
     */
    @ApiStatus.Internal
    private static final
    Multimap<RegistryKey<?>, SortingRule<?>> RULES = HashMultimap.create();
    private static final
    Map<RegistryKey<?>, Multimap<?, ?>> VALUE_TO_FOLLOWER_CACHES = new HashMap<>();
    /**
     * <p>用于物品栏迭代物品迭代时，缓存的物品列表。
     * <p>当配置中的排序影响范围为注册表时，这个字段不会使用。只有当排序影响范围为 {@linkplain SortingInfluenceRange#INVENTORY_ONLY} 时，且排序计算类型为 {@linkplain SortingCalculationType#STANDARD} 时，这个字段才会使用。
     * <p>当游戏初始化时、加载或更改配置时，这个字段会被设为 {@code null}，当下一次打开物品栏时，这个字段就会被赋值。
     */
    public static @Nullable Iterable<Item> cachedInventoryItems;
  }
}
