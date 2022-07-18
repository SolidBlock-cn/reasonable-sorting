package pers.solid.mod;

import net.fabricmc.api.ModInitializer;

public class ReasonableSortingQuilt implements ModInitializer {

  @Override
  public void onInitialize() {
    SortingRules.initialize();
    TransferRules.initialize();
    QuiltConfigs.QUILT_CONFIG.save();
  }
}
