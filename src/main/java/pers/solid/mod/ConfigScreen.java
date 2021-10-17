package pers.solid.mod;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen implements ModMenuApi {
    String currentValue = "";
    List<String> currentList = new ArrayList<>();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createScreen;
    }

    private Screen createScreen(Screen previousScreen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(previousScreen)
                .setTitle(new TranslatableText("title.reasonable-sorting.config"));
        builder.setSavingRunnable(() -> {
            // Serialise the config into the config file. This will be called last after all variables are updated.
        });

        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.examplemod.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder.startStrField(new TranslatableText("option.examplemod.optionA"), currentValue)
                .setDefaultValue("This is the default value") // Recommended: Used when user click "Reset"
                .setTooltip(new TranslatableText("This option is awesome!")) // Optional: Shown when the user hover over this option
                .setSaveConsumer(newValue -> currentValue = newValue) // Recommended: Called when user save the config
                .build()); // Builds the option entry for cloth config

        general.addEntry(entryBuilder
                .startStrList(new TranslatableText("options.examplemod.option_list"), currentList)
                .setDefaultValue(List.of("a", "b", "c"))
                .setAddButtonTooltip(Text.of("options.examplemod.add"))
                        .setSaveConsumer(newValue -> currentList = newValue)
                .build());

        general.addEntry(entryBuilder
                .startStrField(new TranslatableText("str"),"")
                .setDefaultValue("str")
                .build());

        return builder.build();
    }
}
