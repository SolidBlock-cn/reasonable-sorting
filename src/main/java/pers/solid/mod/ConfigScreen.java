package pers.solid.mod;

import com.google.common.collect.ImmutableList;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class ConfigScreen implements ModMenuApi {
    public static final Map<Item, Collection<Item>> CUSTOM_SORTING_RULES = new HashMap<>();
    static List<String> customSortingRulesStrList = new ArrayList<>();
    static boolean buttonsInDecorations = false;
    static boolean fenceGatesInDecorations = false;
    static boolean swordsInTools = false;

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

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createScreen;
    }

    private Screen createScreen(Screen previousScreen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(previousScreen)
                .setTitle(new TranslatableText("title.reasonable-sorting.config"));

        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.reasonable-sorting.custom_sorting_rules"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 自定义排序规则列表
        general.addEntry(entryBuilder
                .startStrList(new TranslatableText("options.reasonable-sorting.custom_sorting_rules"), customSortingRulesStrList)
                .setDefaultValue(List.of("# Example: minecraft:stick minecraft:iron_pickaxe"))
                .setAddButtonTooltip(new TranslatableText("options.reasonable-sorting.custom-sorting-rules.add"))
                .setSaveConsumer(list -> {
                    customSortingRulesStrList = formatted(list);
                    CUSTOM_SORTING_RULES.clear();
                    CUSTOM_SORTING_RULES.putAll(fromStringList(list));
                    MixinHelper.compileItemGroupTransferRules(MixinHelper.ITEM_GROUP_TRANSFER_RULES);
                }).build());

        general.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("options.reasonable-sorting.buttons_in_decorations"), buttonsInDecorations)
                .setSaveConsumer(b -> buttonsInDecorations = b)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("options.reasonable-sorting.fence_gates_in_decorations"), fenceGatesInDecorations)
                .setSaveConsumer(b -> fenceGatesInDecorations = b)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("options.reasonable-sorting.swords_in_tools"), swordsInTools)
                .setSaveConsumer(b -> swordsInTools = b)
                .build());

        return builder.build();
    }
}
