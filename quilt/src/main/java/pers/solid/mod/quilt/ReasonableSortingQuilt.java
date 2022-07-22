package pers.solid.mod.quilt;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ModInitializer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.solid.mod.ConfigScreen;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

public class ReasonableSortingQuilt implements ModInitializer, ModMenuApi {
  public static final ConfigScreen CONFIG_SCREEN = new ConfigScreen();
  public static final Logger LOGGER = LoggerFactory.getLogger(ReasonableSortingQuilt.class);

  @Override
  public void onInitialize() {
    SortingRules.initialize();
    TransferRules.initialize();
    switch (MinecraftQuiltLoader.getEnvironmentType()) {
      case CLIENT -> ClientLifecycleEvents.READY.register(client -> Configs.loadAndUpdate());
      case SERVER -> ServerLifecycleEvents.READY.register(server -> Configs.loadAndUpdate());
    }
  }

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return CONFIG_SCREEN::createScreen;
  }
}
