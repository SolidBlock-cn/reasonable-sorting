package pers.solid.mod.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import pers.solid.mod.ConfigScreen;

public class ConfigScreenFabric implements ModMenuApi {
  public static final ConfigScreen INSTANCE = new ConfigScreen();

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return INSTANCE::createScreen;
  }
}
