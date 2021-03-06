package pers.solid.mod.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

public class ReasonableSortingFabric implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(ReasonableSortingFabric.class);

  @Override
  public void onInitialize() {
    SortingRules.initialize();
    TransferRules.initialize();
    switch (FabricLoader.getInstance().getEnvironmentType()) {
      case CLIENT -> ClientLifecycleEvents.CLIENT_STARTED.register(client -> Configs.loadAndUpdate());
      case SERVER -> ServerLifecycleEvents.SERVER_STARTED.register(server -> Configs.loadAndUpdate());
    }
  }
}
