package pers.solid.mod.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.solid.mod.ConfigScreen;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

@Mod("reasonable_sorting")
public class ReasonableSortingForge {
  public static final Logger LOGGER = LoggerFactory.getLogger(ReasonableSortingForge.class);

  static {
    Configs.instance = new Configs();
  }

  public ReasonableSortingForge() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOWEST, (RegisterEvent event) -> event.register(ForgeRegistries.Keys.ITEMS, helper -> {
      SortingRules.initialize();
      TransferRules.initialize();
      Configs.loadAndUpdate();
    }));

    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, screen) -> new ConfigScreen().createScreen(screen))));
  }
}
