package pers.solid.mod.quilt;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.solid.mod.ConfigScreen;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

public class ReasonableSortingQuilt implements ModInitializer, ModMenuApi {
  public static final ConfigScreen CONFIG_SCREEN = new ConfigScreen();
  public static final Logger LOGGER = LogManager.getLogger(ReasonableSortingQuilt.class);

  @Override
  public void onInitialize() {
    SortingRules.initialize();
    TransferRules.initialize();
    Configs.loadAndUpdate();
  }

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return CONFIG_SCREEN::createScreen;
  }
}
