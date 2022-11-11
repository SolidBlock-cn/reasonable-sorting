package pers.solid.mod.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

public class ReasonableSortingQuilt implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(ReasonableSortingQuilt.class);

  @Override
  public void onInitialize(ModContainer mod) {
    SortingRules.initialize();
    TransferRules.initialize();
    switch (MinecraftQuiltLoader.getEnvironmentType()) {
      case CLIENT -> ClientLifecycleEvents.READY.register(client -> Configs.loadAndUpdate());
      case SERVER -> ServerLifecycleEvents.READY.register(server -> Configs.loadAndUpdate());
    }
  }
}
