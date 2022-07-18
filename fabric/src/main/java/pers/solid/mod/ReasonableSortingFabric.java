package pers.solid.mod;

import net.fabricmc.api.ModInitializer;

public class ReasonableSortingFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    SortingRules.initialize();
    TransferRules.initialize();
    FabricConfigs.CONFIG_HOLDER.load();
  }
}
