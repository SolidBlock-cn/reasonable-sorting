package pers.solid.mod;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class ConfigScreen implements ModMenuApi {
    public static final Map<Item, Collection<Item>> CUSTOM_SORTING_RULES = new HashMap<>();
    public static final Map<Item, ItemGroup> CUSTOM_TRANSFER_RULE = new LinkedHashMap<>();
    public static final Map<Pattern, ItemGroup> CUSTOM_REGEX_TRANSFER_RULE = new LinkedHashMap<>();
    public static final Map<Item, ItemGroup> ABSTRACT_CUSTOM_REGEX_TRANSFER_RULE = new AbstractMap<>() {
        @Override
        public ItemGroup get(Object key) {
            if (key instanceof Item) {
                final Identifier id = Registry.ITEM.getId((Item) key);
                if (id == Registry.ITEM.getDefaultId()) return null;
                final String idString = id.toString();
                for (Entry<Pattern, ItemGroup> entry : CUSTOM_REGEX_TRANSFER_RULE.entrySet()) {
                    if (entry.getKey().matcher(idString).matches()) return entry.getValue();
                }
            }
            return null;
        }

        @Override
        @Deprecated
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        @Override
        public boolean containsValue(Object value) {
            return CUSTOM_REGEX_TRANSFER_RULE.containsValue(value);
        }

        @NotNull
        @Override
        public Set<Entry<Item, ItemGroup>> entrySet() {
            ImmutableSet.Builder<Entry<Item, ItemGroup>> builder = new ImmutableSet.Builder<>();
            for (Item item : Registry.ITEM) {
                final ItemGroup itemGroup = get(item);
                if (itemGroup != null) builder.add(new SimpleImmutableEntry<>(item, itemGroup));
            }
            return builder.build();
        }
    };
    public static final Map<BlockFamily.Variant, ItemGroup> CUSTOM_VARIANT_TRANSFER_RULE = new LinkedHashMap<>();
    public static final Map<Item, ItemGroup> ABSTRACT_CUSTOM_VARIANT_TRANSFER_RULE = new AbstractMap<>() {
        @Override
        public Set<Entry<Item, ItemGroup>> entrySet() {
            return (Registry.ITEM.stream().map(item -> new AbstractMap.SimpleImmutableEntry<>(item, get(item))).collect(Collectors.toUnmodifiableSet()));
        }

        @Override
        public ItemGroup get(Object key) {
            if (!(key instanceof BlockItem)) return null;
            for (BlockFamily blockFamily : BlockFamilyRule.BASE_BLOCKS_TO_FAMILIES.values())
                for (Map.Entry<BlockFamily.Variant, ItemGroup> entry : CUSTOM_VARIANT_TRANSFER_RULE.entrySet()) {
                    final BlockFamily.Variant variant = entry.getKey();
                    if (blockFamily.getVariant(variant) == ((BlockItem) key).getBlock()) return entry.getValue();
                }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) == null;
        }
    };

    public static final Map<String, BlockFamily.Variant> NAME_TO_VARIANT = Arrays.stream(BlockFamily.Variant.values()).collect(ImmutableMap.toImmutableMap(BlockFamily.Variant::getName, variant -> variant));

    public static Map<Item, Collection<Item>> getCustomSortingRules() {
        return CUSTOM_SORTING_RULES;
    }

    public static List<String> toStringList(Map<Item, Collection<Item>> map) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<Item, Collection<Item>> entry : map.entrySet()) {
            final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
            final Identifier id = Registry.ITEM.getId(entry.getKey());
            if (id == Registry.ITEM.getDefaultId()) continue;
            builder.add(id.toString());
            for (Item item : entry.getValue()) {
                final Identifier id1 = Registry.ITEM.getId(item);
                if (id1 == Registry.ITEM.getDefaultId()) continue;
                builder.add(id1.toString());
            }
            list.add(String.join(" ", builder.build()));
        }
        return list;
    }

    public static List<String> formatted(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(String.join(" ", Arrays.stream(s.split("\\s+")).map(Identifier::tryParse).filter(Objects::nonNull).map(Identifier::toString).toList()));
        }
        return newList;
    }

    public static Map<Item, Collection<Item>> fromStringList(List<String> list) {
        Map<Item, Collection<Item>> map = new LinkedHashMap<>();
        for (String s : list) {
            final var split = new ArrayList<>(Arrays.asList(s.split("\\s+")));
            if (split.size() < 1) continue;
            var key = Identifier.tryParse(split.remove(0));
            if (key == null) continue;
            if (Registry.ITEM.containsId(key))
                map.put(Registry.ITEM.get(key), (split.stream().map(Identifier::tryParse).filter(Registry.ITEM::containsId).map(Registry.ITEM::get).toList()));
        }
        return map;
    }

    public static Map<Item, ItemGroup> getCustomTransferRule() {
        return CUSTOM_TRANSFER_RULE;
    }

    public static Map<Item, ItemGroup> getAbstractCustomRegexTransferRule() {
        return ABSTRACT_CUSTOM_REGEX_TRANSFER_RULE;
    }

    public static Map<Item, ItemGroup> getAbstractCustomVariantTransferRule() {
        return ABSTRACT_CUSTOM_VARIANT_TRANSFER_RULE;
    }

    private static @Nullable ItemGroup getGroupFromId(String id) {
        for (ItemGroup group : ItemGroup.GROUPS) {
            if (Objects.equals(group.getName(), id)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createScreen;
    }

    private Screen createScreen(Screen previousScreen) {
        final Configs config = Configs.CONFIG_HOLDER.getConfig();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(previousScreen)
                .setSavingRunnable(() -> {
                    MixinHelper.compileItemGroupTransferRules(MixinHelper.ITEM_GROUP_TRANSFER_RULES);
                    Configs.CONFIG_HOLDER.save();
                })
                .setTitle(new TranslatableText("title.reasonable-sorting.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory categorySorting = builder.getOrCreateCategory(new TranslatableText("category.reasonable-sorting.sorting"));
        categorySorting.setDescription(new TranslatableText[]{new TranslatableText("category.reasonable-sorting.sorting.description")});
        ConfigCategory categoryTransfer = builder.getOrCreateCategory(new TranslatableText("category.reasonable-sorting.transfer"));
        categoryTransfer.setDescription(new TranslatableText[]{new TranslatableText("category.reasonable-sorting.sorting.transfer")});

        // 排序部分。
        categorySorting.addEntry(entryBuilder
                .startTextDescription(new TranslatableText("category.reasonable-sorting.sorting.description"))
                .build());

        categorySorting.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.enable_sorting"), config.enableSorting)
                .setTooltip(new TranslatableText("option.reasonable-sorting.enable_sorting.tooltip"))
                .setYesNoTextSupplier(b -> new TranslatableText(b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
                .setDefaultValue(true)
                .setSaveConsumer(b -> config.enableSorting = b)
                .build());

        categorySorting.addEntry(entryBuilder
                .startStrList(new TranslatableText("option.reasonable-sorting.custom_sorting_rules"), config.customSortingRulesStrList)
                .setTooltip(new TranslatableText("option.reasonable-sorting.custom_sorting_rules.tooltip"), new TranslatableText("option.reasonable-sorting.custom_sorting_rules.example"))
                .setInsertInFront(false)
                .setExpanded(true)
                .setAddButtonTooltip(new TranslatableText("option.reasonable-sorting.custom_sorting_rules.add"))
                .setRemoveButtonTooltip(new TranslatableText("option.reasonable-sorting.custom_sorting_rule.remove"))
                .setSaveConsumer(list -> {
                    config.customSortingRulesStrList = formatted(list);
                    CUSTOM_SORTING_RULES.clear();
                    CUSTOM_SORTING_RULES.putAll(fromStringList(list));
                }).build());

        categorySorting.addEntry(entryBuilder
                .startStrField(new TranslatableText("option.reasonable-sorting.variants_following_base_blocks"), config.variantsFollowingBaseBlocks)
                .setDefaultValue("stairs slab")
                .setTooltip(new TranslatableText("option.reasonable-sorting.variants_following_base_blocks.tooltip"), new TranslatableText("option.reasonable-sorting.variants_following_base_blocks.example", String.join(" ", Arrays.stream(BlockFamily.Variant.values()).map(BlockFamily.Variant::getName).toList())))
                .setErrorSupplier(s -> {
                    List<String> invalidNames = new ArrayList<>();
                    Arrays.stream(s.split("\\s+")).filter(name -> !name.isEmpty()).filter(name -> !NAME_TO_VARIANT.containsKey(name)).forEach(invalidNames::add);
                    return invalidNames.isEmpty() ? Optional.empty() : Optional.of(new TranslatableText("option.reasonable-sorting.variants_following_base_blocks.invalid_name", String.join(", ", invalidNames)));
                })
                .setSaveConsumer(s -> {
                    config.variantsFollowingBaseBlocks = s;
                    BlockFamilyRule.AFFECTED_VARIANTS.clear();
                    Arrays.stream(s.split("\\s+")).filter(name -> !name.isEmpty()).map(NAME_TO_VARIANT::get).filter(Objects::nonNull).forEach(BlockFamilyRule.AFFECTED_VARIANTS::add);
                })
                .build());

        categorySorting.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.fence_gate_follows_fence"), config.fenceGateFollowsFence)
                .setSaveConsumer(b -> config.fenceGateFollowsFence = b)
                .setTooltip(new TranslatableText("option.reasonable-sorting.fence_gate_follows_fence.tooltip"))
                .build());


        // 物品组转移部分。
        categoryTransfer.addEntry(entryBuilder.startTextDescription(new TranslatableText("category.reasonable-sorting.transfer.description"))
                .build());

        categoryTransfer.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.enable_group_transfer"), config.enableGroupTransfer)
                .setDefaultValue(true)
                .setTooltip(new TranslatableText("option.reasonable-sorting.enable_group_transfer.tooltip"))
                .setYesNoTextSupplier(b -> new TranslatableText(b ? "text.reasonable-sorting.enabled" : "text.reasonable-sorting.disabled"))
                .setSaveConsumer(b -> config.enableGroupTransfer = b)
                .build());

        categoryTransfer.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.buttons_in_decorations"), config.buttonsInDecorations)
                .setSaveConsumer(b -> config.buttonsInDecorations = b)
                .build());
        categoryTransfer.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.fence_gates_in_decorations"), config.fenceGatesInDecorations)
                .setSaveConsumer(b -> config.fenceGatesInDecorations = b)
                .build());
        categoryTransfer.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.swords_in_tools"), config.swordsInTools)
                .setSaveConsumer(b -> {
                    config.swordsInTools = b;
                })
                .build());
        categoryTransfer.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.reasonable-sorting.doors_in_decorations"), config.doorsInDecorations)
                .setSaveConsumer(b -> config.doorsInDecorations = b)
                .build());

        categoryTransfer.addEntry(entryBuilder
                .startTextDescription(new TranslatableText("option.reasonable-sorting.describe_item_groups", String.join(" ", Arrays.stream(ItemGroup.GROUPS).map(ItemGroup::getName).toList())))
                .build());

        categoryTransfer.addEntry(entryBuilder
                .startStrList(new TranslatableText("option.reasonable-sorting.custom_transfer_rules"), config.transferRules)
                .setTooltip(new TranslatableText("option.reasonable-sorting.custom_transfer_rules.tooltip"))
                .setExpanded(true)
                .setInsertInFront(false)
                .setSaveConsumer(list -> {
                    config.transferRules = list;
                    CUSTOM_TRANSFER_RULE.clear();
                    for (String s : list) {
                        final String[] split = s.split("\\s+");
                        if (split.length < 2) continue;
                        final Identifier id = Identifier.tryParse(split[0]);
                        if (!Registry.ITEM.containsId(id)) continue;
                        final Item item = Registry.ITEM.get(id);
                        ItemGroup itemGroup = getGroupFromId(split[1]);
                        CUSTOM_TRANSFER_RULE.put(item, itemGroup);
                    }
                })
                .build());

        categoryTransfer.addEntry(entryBuilder
                .startStrList(new TranslatableText("option.reasonable-sorting.custom_variant_transfer_rules"), config.variantTransferRules)
                .setTooltip(new TranslatableText("option.reasonable-sorting.custom_variant_transfer_rules.tooltip"))
                .setExpanded(true)
                .setInsertInFront(false)
                .setCellErrorSupplier(s -> {
                    if (s.isEmpty()) return Optional.empty();
                    final String[] split = s.split("\\s+");
                    if (split.length < 2)
                        return Optional.of(new TranslatableText("option.reasonable-sorting.error.group_name_expected"));
                    final ItemGroup group = getGroupFromId(split[1]);
                    if (group == null)
                        return Optional.of(new TranslatableText("option.reasonable-sorting.error.invalid_group_id", split[1]));
                    if (!NAME_TO_VARIANT.containsKey(split[0]))
                        return Optional.of(new TranslatableText("option.reasonable-sorting.error.invalid_variant_name", split[0]));
                    return Optional.empty();
                })
                .setSaveConsumer(list -> {
                    config.variantTransferRules = list;
                    CUSTOM_TRANSFER_RULE.clear();
                    for (String s : list) {
                        try {
                            final String[] split = s.split("\\s+");
                            if (split.length < 2) continue;
                            final BlockFamily.Variant variant = NAME_TO_VARIANT.get(split[0]);
                            final ItemGroup group = getGroupFromId(split[1]);
                            CUSTOM_VARIANT_TRANSFER_RULE.put(Objects.requireNonNull(variant), Objects.requireNonNull(group));
                        } catch (NullPointerException ignored) {
                        }
                    }
                })
                .build());

        categoryTransfer.addEntry(entryBuilder
                .startStrList(new TranslatableText("option.reasonable-sorting.custom_regex_transfer_rules"), config.regexTransferRules)
                .setTooltip(new TranslatableText("option.reasonable-sorting.custom_regex_transfer_rules.tooltip"))
                .setExpanded(true)
                .setInsertInFront(false)
                .setCellErrorSupplier(s -> {
                    if (s.isEmpty()) return Optional.empty();
                    final String[] split = s.split("\\s+");
                    if (split.length < 2)
                        return Optional.of(new TranslatableText("option.reasonable-sorting.error.group_name_expected"));
                    final ItemGroup group = getGroupFromId(split[1]);
                    if (group == null)
                        return Optional.of(new TranslatableText("option.reasonable-sorting.error.invalid_group_id", split[1]));
                    try {
                        Pattern.compile(split[0]);
                    } catch (PatternSyntaxException e) {
                        return Optional.of(new TranslatableText("option.reasonable-sorting.error.invalid_regex", split[0], e.getMessage()));
                    }
                    return Optional.empty();
                })
                .setSaveConsumer(list -> {
                    config.regexTransferRules = list;
                    CUSTOM_REGEX_TRANSFER_RULE.clear();
                    for (String s : list)
                        try {
                            final String[] split = s.split("\\s+");
                            if (split.length < 2) continue;
                            final Pattern compile = Pattern.compile(split[0]);
                            final ItemGroup group = getGroupFromId(split[1]);
                            CUSTOM_REGEX_TRANSFER_RULE.put(Objects.requireNonNull(compile), Objects.requireNonNull(group));
                        } catch (NullPointerException | PatternSyntaxException ignored) {
                        }
                })
                .build());

        return builder.build();
    }
}
