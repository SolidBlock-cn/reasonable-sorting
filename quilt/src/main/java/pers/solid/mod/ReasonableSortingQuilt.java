package pers.solid.mod;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ReasonableSortingQuilt implements ModInitializer {

  @Override
  public void onInitialize(ModContainer mod) {
    SortingRules.initialize();
    TransferRules.initialize();
    QuiltConfigs.QUILT_CONFIG.save();
  }
}
