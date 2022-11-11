package pers.solid.mod.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.solid.mod.ConfigScreen;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

@Mod("reasonable_sorting")
public class ReasonableSortingForge {
  public static final Logger LOGGER = LogManager.getLogger(ReasonableSortingForge.class);

  public ReasonableSortingForge() {
    SortingRules.initialize();
    TransferRules.initialize();
    FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLLoadCompleteEvent event) -> Configs.loadAndUpdate()
    );

    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, screen) -> new ConfigScreen().createScreen(screen)));
  }
}
