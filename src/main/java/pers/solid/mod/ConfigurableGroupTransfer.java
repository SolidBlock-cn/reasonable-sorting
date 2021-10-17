package pers.solid.mod;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ConfigurableGroupTransfer {
    public static final Map<Item, ItemGroup> CUSTOM_TRANSFER_RULE = new LinkedHashMap<>();
    public static final Map<Pattern,ItemGroup> CUSTOM_REGEX_TRANSFER_RULE = new LinkedHashMap<>();

    public static Map<Item,ItemGroup> getCustomTransferRule() {
        return CUSTOM_TRANSFER_RULE;
    }

    public static Map<Item,ItemGroup> getAbstractCustomRegexTransferRule() {
        return ABSTRACT_CUSTOM_REGEX_TRANSFER_RULE;
    }

    public static final Map<Item,ItemGroup> ABSTRACT_CUSTOM_REGEX_TRANSFER_RULE = new AbstractMap<>() {
        @Override
        public ItemGroup get(Object key) {
            if (key instanceof Item) {
                final Identifier id = Registry.ITEM.getId((Item) key);
                if (id==Registry.ITEM.getDefaultId()) return null;
                final String idString = id.toString();
                for (Entry<Pattern, ItemGroup> entry : CUSTOM_REGEX_TRANSFER_RULE.entrySet()) {
                    if (entry.getKey().matcher(idString).matches()) return entry.getValue();
                }
            }
            return null;
        }

        @Override@Deprecated
        public boolean containsKey(Object key) {
            return get(key)!=null;
        }

        @Override
        public boolean containsValue(Object value) {
            return CUSTOM_REGEX_TRANSFER_RULE.containsValue(value);
        }

        @NotNull
        @Override
        public Set<Entry<Item, ItemGroup>> entrySet() {
            ImmutableSet.Builder<Entry<Item,ItemGroup>> builder = new ImmutableSet.Builder<>();
            for (Item item : Registry.ITEM) {
                final ItemGroup itemGroup = get(item);
                if (itemGroup!=null) builder.add(new SimpleImmutableEntry<>(item,itemGroup));
            }
            return builder.build();
        }
    };
}
