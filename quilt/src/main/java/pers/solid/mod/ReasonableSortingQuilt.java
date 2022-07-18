package pers.solid.mod;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReasonableSortingQuilt implements ModInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReasonableSortingQuilt.class);

  @Override
  public void onInitialize(ModContainer mod) {
    SortingRules.initialize();
    TransferRules.initialize();
    QuiltConfigs.QUILT_CONFIG.save();
  }
}
